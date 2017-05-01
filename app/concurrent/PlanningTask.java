package concurrent;

import planner.elements.TupleCollection;
import javafx.concurrent.Task;
import planner.VoiceOutputPlan;
import planner.VoicePlanner;

/**
 * Task for running voice planning operations in the background.
 */
public class PlanningTask extends Task<VoiceOutputPlan> {
    TupleCollection tupleCollection;
    VoicePlanner planner;

    /**
     * Primary constructor for a PlanningTask
     * @param tupleCollection The collection of tuples to construct a plan for
     * @param planner The planner to use in planning
     */
    public PlanningTask(TupleCollection tupleCollection, VoicePlanner planner) {
        this.tupleCollection = tupleCollection;
        this.planner = planner;
    }

    @Override
    protected VoiceOutputPlan call() throws Exception {
        return planner.plan(tupleCollection);
    }
}
