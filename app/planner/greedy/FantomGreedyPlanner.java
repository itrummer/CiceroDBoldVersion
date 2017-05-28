package planner.greedy;

import planner.VoiceOutputPlan;
import planner.elements.*;
import planner.naive.NaiveVoicePlanner;

import java.util.*;

public class FantomGreedyPlanner extends GreedyPlanner {
    public static final int P = 2;

    @Override
    public VoiceOutputPlan executeAlgorithm(TupleCollection tupleCollection) {
        return super.executeAlgorithm(tupleCollection);
    }

    public void executeFANTOM(TupleCollection tuples, Set<ValueDomain> domains) {
        int M = 0;
        for (ValueDomain domain : domains) {
            M = Math.max(M, timeSavingsFromSingleDomain(tuples, domain));
        }

        double gamma = (2 * P * M) / (double) ((P + 1) * (2 * P + 1));

        Set<ValueDomain> U = new HashSet<>();

        // TODO: what is n...
        int n = 1;
        for (int i = 0; i < n; i++) {
            double rho = gamma * i;
            Set<ValueDomain> S = iteratedGreedyWithDensityThreshold(tuples, rho, domains);
            U.addAll(S);
        }

    }

    public int timeSavingsFromSingleDomain(TupleCollection tuples, ValueDomain domain) {
        int savings = 0;
        for (Tuple t : tuples) {
            savings += t.timeSavingsFromValueDomain(domain);
        }
        return savings;
    }

    /**
     * Runs the Greedy With Density Theshold Algorithm multiple times to produce multiple solutions for a ValueDomain
     * set. Returns the set of all produced solutions with the maximum savings
     *
     * @param tuples A collection of tuples to be used to compute the submodular utility function
     * @param rho The density threshold
     * @param domains A set of candidate ValueDomains
     * @return A set of ValueDomains satisfying the independences of attributes and the context size constraint
     */
    public Set<ValueDomain> iteratedGreedyWithDensityThreshold(TupleCollection tuples, double rho, Set<ValueDomain> domains) {
        Set<ValueDomain> remainingDomains = new HashSet<>(domains);
        Set<Set<ValueDomain>> U = new HashSet<>();

        for (int i = 0; i <= P + 1; i++) {
            Set<ValueDomain> S = greedyWithDensityThreshold(tuples, rho, remainingDomains);
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
     * @param rho The minimum density threshold
     * @param domains The candidate domains from which to greedily construct a domain set
     * @return A set of domains with at most one ValueDomain corresponding to each attribute and at most mS domains
     */
    public Set<ValueDomain> greedyWithDensityThreshold(TupleCollection tuples, double rho, Set<ValueDomain> domains) {
        Set<ValueDomain> S = new HashSet<>();
        Set<String> SAttributes = new HashSet<>();
        int savingsFromS = timeGainFromValueDomains(tuples, S);

        // greedy selection process
        for (int i = 0; i < config.getMaxContextSize(); i++) {
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
                if (meetsDensityThreshold(marginalSavings, SWithElement.size(), rho) && marginalSavings > bestMarginalSavings) {
                    bestMarginalSavings = marginalSavings;
                    selection = d;
                }
            }

            if (selection == null) {
                // we can assume that we will not reach the max context size and can exit early
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
     * @return
     */
    public Set<ValueDomain> unconstrainedSubmodularMaximization(TupleCollection tuples, Set<ValueDomain> domains) {
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
    public int timeGainFromValueDomains(TupleCollection tuples, Set<ValueDomain> domains) {
        if (domains.isEmpty()) {
            return 0;
        }

        Context c = new Context(domains);
        int savings = 0;
        for (Tuple t : tuples) {
            savings += t.toSpeechText(true).length() - t.toSpeechText(c, true).length();
        }

        return savings;
    }

    /**
     * Determines whether the marginal savings gained from adding an element to a ValueDomain set
     * proportional to the cost of the knapsack meets a minimum density threshold.
     *
     * @param marginalSavings The time savings gained from adding an element
     * @param setSize The number of ValueDomains in the set with an additional element
     * @param rho A density threshold
     * @return
     */
    public boolean meetsDensityThreshold(int marginalSavings, int setSize, double rho) {
        double costPerValueDomain = 1.0 / config.getMaxContextSize();
        double knapsackCost = setSize * costPerValueDomain;
        return marginalSavings /knapsackCost >= rho;
    }
    
}
