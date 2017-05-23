package planner;

import planner.elements.TupleCollection;

/**
 * Abstract representation of VoicePlanners
 */
public abstract class VoicePlanner {

    /**
     * Visits a TupleCollection to construct a VoiceOutputPlan that represents the contents of rowCollection
     * @param tupleCollection
     * @return The VoiceOutputPlan for the TupleCollection
     */
    public PlanningResult plan(TupleCollection tupleCollection) {
        return plan(tupleCollection, 1);
    }

    /**
     * Executes the planning algorithm of this VoicePlanner. Allows specification of how many times the
     * algorithm should be executed.
     * @param tupleCollection
     * @param n The number of times to execute the algorithm
     * @return
     */
    public PlanningResult plan(TupleCollection tupleCollection, int n) {
        long startTime = System.currentTimeMillis();
        VoiceOutputPlan plan = null;
        for (int i = 0; i < n; i++) {
            plan = executeAlgorithm(tupleCollection);
        }
        long endTime = System.currentTimeMillis();
        return new PlanningResult(plan, endTime - startTime, n, false);
    }

    protected abstract VoiceOutputPlan executeAlgorithm(TupleCollection tupleCollection);

    public abstract ToleranceConfig getConfig();

    public abstract String getPlannerName();

}
