package planner;

/**
 * Contains information about the execution results of a VoicePlanner
 */
public class PlanningResult {
    VoiceOutputPlan plan;
    long totalExecutionTimeMillis;
    int executionCount;
    boolean timedOut;

    public PlanningResult(VoiceOutputPlan plan, long totalExecutionTimeMillis, int n, boolean timedOut) {
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

    public long getAverageExecutionTimeMillis() {
        return totalExecutionTimeMillis / executionCount;
    }

    public double getAverageExecutionTimeSeconds() {
        return getAverageExecutionTimeMillis() / 1000.0;
    }

}
