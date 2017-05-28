package planner;

/**
 * Contains information about the execution results of a VoicePlanner
 */
public class PlanningResult {
    VoiceOutputPlan plan;
    String plannerName;
    String relation;
    long totalExecutionTimeMillis;
    int executionCount;
    boolean timedOut;
    int columns;
    int tuples;
    int mS;
    double mW;
    int mC;

    public PlanningResult(VoiceOutputPlan plan, long totalExecutionTimeMillis, int n, boolean timedOut) {
        this.plan = plan;
        this.totalExecutionTimeMillis = totalExecutionTimeMillis;
        this.executionCount = n;
        this.timedOut = timedOut;
    }

    public PlanningResult(VoiceOutputPlan plan, long totalExecutionTimeMillis, boolean timedOut, int columns, int tuples, VoicePlanner planner, String relation) {
        this.plan = plan;
        this.plannerName = planner.getPlannerName();
        this.totalExecutionTimeMillis = totalExecutionTimeMillis;
        this.executionCount = 1;
        this.timedOut = timedOut;
        this.relation = relation;
        this.columns = columns;
        this.tuples = tuples;
        this.mS = planner.getConfig().getMaxContextSize();
        this.mW = planner.getConfig().getMaxNumericalDomainWidth();
        this.mC = planner.getConfig().getMaxCategoricalDomainSize();
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

    public int getTuples() {
        return tuples;
    }

    public String getCSVLine() {
        return relation +
                "," + plannerName +
                "," + getAverageExecutionTimeMillis() +
                "," + timedOut +
                "," + plan.toSpeechText(true).length() +
                "," + columns +
                "," + tuples +
                "," + mS +
                "," + mW +
                "," + mC;
    }

    public static String getCSVHeader() {
        return "relation" +
                ",planner" +
                ",execution time" +
                ",timed out" +
                ",speech cost (characters)" +
                ",number columns" +
                ",number tuples" +
                ",mS" +
                ",mW" +
                ",mC";
    }

    public String getFileNameBase() {
        return relation + "_" + plannerName + "_" + columns + "cols" + "_" + tuples + "tuples" + "__mS_" + mS + "__mW_" + (int) mW + "__mC_" + mC;
    }

}
