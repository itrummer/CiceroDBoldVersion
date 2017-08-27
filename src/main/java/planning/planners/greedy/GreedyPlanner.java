package planning.planners.greedy;

import planning.config.Config;
import planning.planners.naive.NaiveVoicePlanner;
import planning.VoiceOutputPlan;
import planning.elements.*;

import java.util.*;

/**
 * The GreedyPlanner constructs VoiceOutputPlans according to the greedy algorithm. The greedy algorithm proceeds in
 * two stages. First, for each context set, it generates the best buildPlan that uses only the context candidates in the set.
 * Second, it returns the buildPlan with minimal run time among all generated plans
 */
public class GreedyPlanner extends NaiveVoicePlanner {

    public GreedyPlanner() {

    }

    /**
     * Constructs
     * @param tupleCollection
     * @return
     */
    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection, Config config) {
        ArrayList<Context> candidateContexts = new ArrayList<>();
        ArrayList<VoiceOutputPlan> plans = new ArrayList<>();

        // add the naive buildPlan, i.e. the best buildPlan when the candidateContexts is empty
        plans.add(minTimePlan(candidateContexts, tupleCollection));

        // up to maximal number of useful contexts
        for (int i = 0; i < tupleCollection.tupleCount()/2; i++) {
            Context newContext = bestContext(candidateContexts, tupleCollection, config);
            if (newContext == null) {
                break;
            }
            candidateContexts.add(newContext);
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
    protected VoiceOutputPlan minTimePlan(List<Context> contextCandidates, TupleCollection tupleCollection) {
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
            scopes.put(c, new Scope(c, new ArrayList<>(), tupleCollection.getTuplesClassName()));
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
            plan.addScope(new Scope(null, unmatchedTuples, tupleCollection.getTuplesClassName()));
        }

        for (Scope s : scopes.values()) {
            if (s.numberTuples() > 0) {
                plan.addScope(s);
            }
        }

        return plan;
    }

    /**
     * Calculates time savings when outputting rows in a TupleCollection within a specified Context.
     * @param c The Context used to save time in outputting rows
     * @param tupleCollection A collection of Tuples
     * @return The time savings from outputting matching rows within Context c
     */
    private int timeSavingsFromContext(Context c, TupleCollection tupleCollection) {
        if (c == null) {
            return Integer.MIN_VALUE;
        }
        int totalSavings = 0;
        for (Tuple t : tupleCollection) {
            if (c.matches(t)) {
                // if t can be output within c, then we save time equal to T(t) - T(t,c)
                int tSavings = t.toSpeechText(true).length() - t.toSpeechText(c, true).length();
                totalSavings += tSavings;
            }
        }
        return totalSavings;
    }

    /**
     * Generates the best Context for Tuples in a TupleCollection that do not match any of the Contexts
     * in a set of Contexts
     * @param contextSet A set of Contexts
     * @param tupleCollection A collection of Tuples
     * @return The best Context
     */
    protected Context bestContext(ArrayList<Context> contextSet, TupleCollection tupleCollection, Config config) {
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

        Context bestContext = null;
        int bestSavings = Integer.MIN_VALUE;

        Map<Integer, Set<ValueDomain>> domains = unmatchedTuples.candidateAssignments(config.getMaxAllowableCategoricalDomainSize(),
                config.getMaxAllowableNumericalDomainWidth());

        // consider each Context that takes a maximum mS number of domain assignments and
        // at most one domain assignment for a given attribute; compare with bestContext;
        List<Context> contexts = new ArrayList<>();

        candidateContextsForDomains(contexts, domains, new HashSet<>(), 1, config.getMaxAllowableContextSize());
        for (Context c : contexts) {
            int savings = timeSavingsFromContext(c, unmatchedTuples);
            if (savings > bestSavings) {
                bestContext = c;
                bestSavings = savings;
            }
        }

        return bestContext;
    }

    public void candidateContextsForDomains(List<Context> result, Map<Integer, Set<ValueDomain>> domains, Set<ValueDomain> subset, int index, int s) {
        if (!domains.containsKey(index) || subset.size() >= s) {
            return;
        }

        // include one of the domains for the currentIndex
        for (ValueDomain d : domains.get(index)) {
            HashSet<ValueDomain> newSet = new HashSet<>(subset);
            newSet.add(d);
            Context newContext = new Context();
            for (ValueDomain v : newSet) {
                newContext.addDomainAssignment(v);
            }
            result.add(newContext);
            candidateContextsForDomains(result, domains, newSet, index + 1, s);
        }

        // skip the domain at the current index
        candidateContextsForDomains(result, domains, subset, index + 1, s);
    }

    @Override
    public String getPlannerIdentifier() {
        return "greedy";
    }

}
