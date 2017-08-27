package planning;

import planning.config.Config;
import planning.elements.TupleCollection;

/**
 * Contains information about the execution results of a VoicePlanner
 */
public class PlanningResult {
    VoiceOutputPlan plan;
    long executionTime;
    TupleCollection tuples;
    Config config;
    String plannerIdentifier;

    public PlanningResult(VoiceOutputPlan plan,
                          TupleCollection tuples,
                          Config config,
                          String plannerIdentifier,
                          long executionTime) {
        this.plan = plan;
        this.tuples = tuples;
        this.config = config;
        this.plannerIdentifier = plannerIdentifier;
        this.executionTime = executionTime;
    }

    public VoiceOutputPlan getPlan() {
        return plan;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public Config getConfig() {
        return config;
    }

    public TupleCollection getTuples() {
        return tuples;
    }

    public String getPlannerIdentifier() {
        return plannerIdentifier;
    }

}
