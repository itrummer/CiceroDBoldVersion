package planner;

import planner.elements.Context;
import planner.elements.Scope;
import planner.elements.Tuple;
import planner.elements.TupleCollection;
import util.DatabaseUtilities;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * The GreedyPlanner constructs VoiceOutputPlans according to the greedy algorithm. The greedy algorithm proceeds in
 * two stages. First, for each context set, it generates the best plan that uses only the context candidates in the set.
 * Second, it returns the plan with minimal run time among all generated plans
 */
public class GreedyPlanner extends VoicePlanner {

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        ArrayList<Context> candidateContexts = new ArrayList<>();
        ArrayList<VoiceOutputPlan> plans = new ArrayList<>();

        // add the naive plan
        plans.add(minTimePlan(candidateContexts, tupleCollection));

        // up to maximal number of useful contexts
        for (int i = 0; i < tupleCollection.tupleCount()/2; i++) {
            Context newContext = bestContext(candidateContexts, tupleCollection);
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
     * Uses a set of Context candidates to generate the fastest output plan for a TupleCollection.
     * @param contextCandidates The set of Contexts to consider when constructing the fastest VoiceOutputPlan
     * @param tupleCollection The collection of tuples for which to plan
     * @return The fastest VoiceOutputPlan that uses some subset of the context candidates
     */
    private VoiceOutputPlan minTimePlan(ArrayList<Context> contextCandidates, TupleCollection tupleCollection) {
        if (contextCandidates.isEmpty()) {
            // if there are no contexts, the best we can do is output the Naive plan
            return new NaiveVoicePlanner().plan(tupleCollection);
        }

        ArrayList<Tuple> unmatchedTuples = new ArrayList<>();
        ArrayList<Tuple> matchedTuples = new ArrayList<>();
        for (Tuple t : tupleCollection.getTuples()) {
            boolean matched = false;
            for (Context c : contextCandidates) {
                if (c.matches(t)) {
                    matchedTuples.add(t);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                unmatchedTuples.add(t);
            }
        }

        Scope emptyScope = new Scope(unmatchedTuples);

        for (Context c : contextCandidates) {
            ArrayList<Tuple> matchingTuples = new ArrayList<>();
            for (Tuple t : matchedTuples) {
                if (c.matches(t)) {
                    matchingTuples.add(t);
                }
            }


        }

        return null;
    }

    /**
     * Calculates time savings when outputting rows in a TupleCollection within a specified Context.
     * @param c The Context used to save time in outputting rows
     * @param tupleCollection A collection of Tuples
     * @return The time savings from outputting matching rows within Context c
     */
    private int timeSavingsFromContext(Context c, TupleCollection tupleCollection) {
        int totalSavings = 0;
        for (Tuple t : tupleCollection.getTuples()) {
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
    private Context bestContext(ArrayList<Context> contextSet, TupleCollection tupleCollection) {
        return null;
    }

    public static void main(String[] args) {
        try {
            TupleCollection tupleCollection = DatabaseUtilities.executeQuery("select * from restaurants;");
            GreedyPlanner planner = new GreedyPlanner();
            VoiceOutputPlan plan = planner.plan(tupleCollection);
            if (plan != null) {
                System.out.println(plan.toSpeechText(false));
            } else {
                System.out.println("Plan was null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
