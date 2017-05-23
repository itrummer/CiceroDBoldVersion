package planner;

/**
 * Contains information about the execution results of a VoicePlanner
 */
public class PlanningResult {
    VoiceOutputPlan plan;
    double totalExecutionTimeMillis;
    int executionCount;
    boolean timedOut;

    public PlanningResult(VoiceOutputPlan plan, double totalExecutionTimeMillis, int n, boolean timedOut) {
        this.plan = plan;
        this.totalExecutionTimeMillis = totalExecutionTimeMillis;
        this.executionCount = n;
        this.timedOut = timedOut;
    }

    public boolean getTimedOut() {
        return timedOut;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public VoiceOutputPlan getPlan() {
        return plan;
    }

    public double getAverageExecutionTimeMillis() {
        return totalExecutionTimeMillis / executionCount;
    }

    public double getAverageExecutionTimeSeconds() {
        return getAverageExecutionTimeMillis() / 1000.0;
    }

}
