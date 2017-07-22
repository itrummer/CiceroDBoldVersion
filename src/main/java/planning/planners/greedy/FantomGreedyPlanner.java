package planning.planners.greedy;

import planning.VoiceOutputPlan;
import planning.config.Config;
import planning.elements.*;
import planning.planners.naive.NaiveVoicePlanner;

import java.util.*;

public class FantomGreedyPlanner extends NaiveVoicePlanner {
    private static final int P = 2;

    public FantomGreedyPlanner() {

    }

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection, Config config) {
        List<Context> candidateContexts = new ArrayList<>();
        List<VoiceOutputPlan> plans = new ArrayList<>();

        plans.add(minTimePlan(candidateContexts, tupleCollection));

        // up to maximal number of useful contexts
        for (int i = 0; i < tupleCollection.tupleCount()/2; i++) {
            Context bestContext = bestContext(candidateContexts, tupleCollection, config);
            if (bestContext == null) {
                break;
            }
            candidateContexts.add(bestContext);
            VoiceOutputPlan bestNewPlan = minTimePlan(candidateContexts, tupleCollection);
            plans.add(bestNewPlan);
        }

        int minCost = Integer.MAX_VALUE;
        VoiceOutputPlan minPlan = null;

        for (VoiceOutputPlan plan : plans) {
            if (plan == null) continue;
            int planCost = plan.toSpeechText(true).length();
            if (planCost < minCost) {
                minCost = planCost;
                minPlan = plan;
            }
        }

        return minPlan;
    }

    /**
     * Uses a set of Context candidates to generate the fastest output buildPlan for a TupleCollection.
     * @param contextCandidates The set of Contexts to consider when constructing the fastest VoiceOutputPlan
     * @param tupleCollection The collection of tuples for which to buildPlan
     * @return The fastest VoiceOutputPlan that uses some subset of the context candidates
     */
    private VoiceOutputPlan minTimePlan(List<Context> contextCandidates, TupleCollection tupleCollection) {
        if (contextCandidates.isEmpty()) {
            return new NaiveVoicePlanner().plan(tupleCollection, null);
        }

        List<Tuple> unmatchedTuples = new ArrayList<>();
        List<Tuple> matchedTuples = new ArrayList<>();

        for (Tuple t : tupleCollection) {
            boolean matched = false;
            Iterator<Context> contextIterator = contextCandidates.iterator();
            while (contextIterator.hasNext() && !matched) {
                Context c = contextIterator.next();
                if (c.matches(t)) {
                    matchedTuples.add(t);
                    matched = true;
                }
            }
            if (!matched) {
                unmatchedTuples.add(t);
            }
        }

        Map<Context, Scope> scopes = new HashMap<>();
        for (Context c : contextCandidates) {
            scopes.put(c, new Scope(c));
        }

        // for each tuple, find the Context it most favors, i.e. the best
        // savings, and add it to the Scope that contains that Context
        for (Tuple t : matchedTuples) {
            Context favoredContext = null;
            int bestSavings = 0;
            for (Context c : contextCandidates) {
                int newSavings = t.toSpeechText(true).length() - t.toSpeechText(c, true).length();
                if (newSavings > bestSavings) {
                    favoredContext = c;
                    bestSavings = newSavings;
                }
            }
            scopes.get(favoredContext).addMatchingTuple(t);
        }

        VoiceOutputPlan plan = new VoiceOutputPlan();

        if (!unmatchedTuples.isEmpty()) {
            plan.addScope(new Scope(unmatchedTuples));
        }

        for (Scope s : scopes.values()) {
            if (s.numberTuples() > 0) {
                plan.addScope(s);
            }
        }

        return plan;
    }

    private Context bestContext(List<Context> contextSet, TupleCollection tupleCollection, Config config) {
        TupleCollection unmatchedTuples = new TupleCollection(tupleCollection.getAttributes());
        for (Tuple t : tupleCollection) {
            boolean unmatched = true;
            for (Context c : contextSet) {
                if (c.matches(t)) {
                    unmatched = false;
                    break;
                }
            }
            if (unmatched) {
                unmatchedTuples.addTuple(t);
            }
        }

        if (unmatchedTuples.tupleCount() == 0) {
            return null;
        }

        Set<ValueDomain> candidateDomains = unmatchedTuples.candidateAssignmentSet(config.getMaxAllowableCategoricalDomainSize(), config.getMaxAllowableNumericalDomainWidth());
        Set<ValueDomain> bestDomains = executeFANTOM(unmatchedTuples, candidateDomains, config);

        if (bestDomains == null || bestDomains.isEmpty()) {
            return null;
        }

        return new Context(bestDomains);
    }

    private Set<ValueDomain> executeFANTOM(TupleCollection tuples, Set<ValueDomain> domains, Config config) {
        int M = 0;
        for (ValueDomain d : domains) {
            Set<ValueDomain> singleDomain = new HashSet<>();
            singleDomain.add(d);
            M = Math.max(M, timeGainFromValueDomains(tuples, singleDomain));
        }

        // generate multiple solutions by running the Iterated Greedy Algorithm on multiple density values
        double gamma = (2 * P * M) / (double) ((P + 1) * (2 * P + 1));
        Set<Set<ValueDomain>> iteratedGreedyResults = new HashSet<>();

        // iterate through densities: { gamma, gamma * (1+epsilon)^1, gamma * (1+epsilon)^2, ..., gamma * n }
        int n = domains.size();
        double currentDensity = gamma;
        while (currentDensity < gamma * n) {
            Set<ValueDomain> S = iteratedGreedyWithDensityThreshold(tuples, currentDensity, domains, config);
            iteratedGreedyResults.add(S);
            currentDensity = currentDensity * (1.0 + config.getEpsilon());
        }

        Set<ValueDomain> result = null;
        int bestSavings = 0;
        for (Set<ValueDomain> domainSet : iteratedGreedyResults) {
            int savings = timeGainFromValueDomains(tuples, domainSet);
            if (savings >= bestSavings) {
                result = domainSet;
                bestSavings = savings;
            }
        }

        return result;
    }

    /**
     * Runs the Greedy With Density Theshold Algorithm multiple times to produce multiple solutions for a ValueDomain
     * set. Returns the set of all produced solutions with the maximum savings
     * @param tuples A collection of tuples to be used to compute the submodular utility function
     * @param density The density threshold
     * @param domains A set of candidate ValueDomains
     * @return A set of ValueDomains satisfying the independences of attributes and the context size constraint
     */
    public Set<ValueDomain> iteratedGreedyWithDensityThreshold(TupleCollection tuples, double density, Set<ValueDomain> domains, Config config) {
        Set<ValueDomain> remainingDomains = new HashSet<>(domains);
        Set<Set<ValueDomain>> U = new HashSet<>();

        for (int i = 0; i <= P + 1; i++) {
            Set<ValueDomain> S = greedyWithDensityThreshold(tuples, density, remainingDomains, config);
            Set<ValueDomain> SPrime = unconstrainedSubmodularMaximization(tuples, S);
            U.add(S);
            U.add(SPrime);
            remainingDomains.removeAll(S);
        }

        int maxSavings = 0;
        Set<ValueDomain> maxSet = null;
        for (Set<ValueDomain> S : U) {
            int newSavings = timeGainFromValueDomains(tuples, S);
            if (newSavings >= maxSavings) {
                maxSavings = newSavings;
                maxSet = S;
            }
        }

        return maxSet;
    }

    /**
     * Runs the greedy algorithm and picks ValueDomains as long as the marginal savings from adding a new ValueDomain
     * proportional to the total cost of the new set of ValueDomains meets a minimum density threshold.
     * @param tuples
     * @param density The minimum density threshold
     * @param domains The candidate domains from which to greedily construct a domain set
     * @return A set of domains with at most one ValueDomain corresponding to each attribute and at most mS domains
     */
    public Set<ValueDomain> greedyWithDensityThreshold(TupleCollection tuples, double density, Set<ValueDomain> domains, Config config) {
        Set<ValueDomain> S = new HashSet<>();
        Set<String> SAttributes = new HashSet<>();
        int savingsFromS = timeGainFromValueDomains(tuples, S);

        // greedy selection process
        for (int i = 0; i < config.getMaxAllowableContextSize(); i++) {
            ValueDomain selection = null;
            int bestMarginalSavings = 0;

            for (ValueDomain d : domains) {
                if (SAttributes.contains(d.getAttribute())) {
                    // ensures that p-system constraints are satisfied
                    continue;
                }

                Set<ValueDomain> SWithElement = new HashSet<>(S);
                SWithElement.add(d);
                int marginalSavings = timeGainFromValueDomains(tuples, SWithElement) - savingsFromS;

                if (meetsDensityThreshold(marginalSavings, SWithElement.size(), density, config) && marginalSavings > bestMarginalSavings) {
                    bestMarginalSavings = marginalSavings;
                    selection = d;
                }
            }

            if (selection == null) {
                // no qualifying domains, exit early
                break;
            }

            S.add(selection);
            SAttributes.add(selection.getAttribute());
            savingsFromS += bestMarginalSavings;
        }

        // calculate best single domain savings
        Set<ValueDomain> bestSingleDomain = null;
        int bestSingleDomainSavings = 0;
        for (ValueDomain d : domains) {
            Set<ValueDomain> singleDomainSet = new HashSet<>();
            singleDomainSet.add(d);
            int newSavings = timeGainFromValueDomains(tuples, singleDomainSet);

            if (newSavings >= bestSingleDomainSavings) {
                bestSingleDomainSavings = newSavings;
                bestSingleDomain = singleDomainSet;
            }
        }

        if (bestSingleDomain != null && bestSingleDomainSavings > savingsFromS) {
            return bestSingleDomain;
        }

        return S;
    }

    /**
     * Runs an unconstrained submodular maximization algorithm on the given ValueDomain sets.
     * @param tuples The tuples to be used to calculate the time savings for domain sets
     * @param domains A domain set satisfying the constraint that no two ValueDomains fix a domain for the same attribute
     * @return A ValueDomain set satisfying matroid and knapsack contraints
     */
    private Set<ValueDomain> unconstrainedSubmodularMaximization(TupleCollection tuples, Set<ValueDomain> domains) {
        Set<ValueDomain> X = new HashSet<>();
        Set<ValueDomain> Y = new HashSet<>(domains);

        for (ValueDomain domain : domains) {
            int XSavings = timeGainFromValueDomains(tuples, X);
            int YSavings = timeGainFromValueDomains(tuples, Y);

            Set<ValueDomain> XWithElement = new HashSet<>(X);
            XWithElement.add(domain);
            int XWithElementSavings = timeGainFromValueDomains(tuples, XWithElement);

            Set<ValueDomain> YWithoutElement = new HashSet<>(Y);
            YWithoutElement.remove(domain);
            int YWithoutElementSavings = timeGainFromValueDomains(tuples, YWithoutElement);

            int a = XWithElementSavings - XSavings;
            int b = YWithoutElementSavings - YSavings;

            if (a >= b) {
                X = XWithElement;
                Y = YWithoutElement;
            } else {
                Y = YWithoutElement;
            }
        }

        return X;
    }

    /**
     * Returns the time gained from using the given ValueDomain set with the collection of Tuples. This is the
     * submodular utility function for our greedy algorithm.
     */
    private int timeGainFromValueDomains(TupleCollection tuples, Set<ValueDomain> domains) {
        if (domains.isEmpty()) {
            return 0;
        }

        Context c = new Context(domains);
        int savings = 0;
        for (Tuple t : tuples) {
            savings += t.toSpeechText(true).length() - t.toSpeechText(c, true).length();
        }

        int contextCost = c.toSpeechText(true).length();

        return savings - contextCost;
    }

    /**
     * Determines whether the marginal savings gained from adding an element to a ValueDomain set
     * proportional to the cost of the knapsack meets a minimum density threshold.
     *
     * @param marginalSavings The time savings gained from adding an element
     * @param setSize The number of ValueDomains in the set with an additional element
     * @param density A density threshold
     * @return
     */
    private boolean meetsDensityThreshold(int marginalSavings, int setSize, double density, Config config) {
        double costPerValueDomain = 1.0 / config.getMaxAllowableContextSize();
        double knapsackCost = setSize * costPerValueDomain;
        return marginalSavings / knapsackCost >= density;
    }

    @Override
    public String getPlannerIdentifier() {
        return "greedy-FANTOM";
    }

}
