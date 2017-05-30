package planner;

import planner.elements.TupleCollection;
import sql.Query;

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
    double entropy;
    Integer naiveCost;
    Integer audioSpeechCost;

    public PlanningResult(VoiceOutputPlan plan) {
        this.plan = plan;
        this.plannerName = "no_name";
        this.timedOut = false;
        this.relation = "no_relation";
        this.naiveCost = null;
        this.audioSpeechCost = null;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public VoiceOutputPlan getPlan() {
        return plan;
    }

    public long getExecutionTimeMillis() {
        return totalExecutionTimeMillis;
    }

    public double getExecutionTimeSeconds() {
        return totalExecutionTimeMillis / 1000.0;
    }

    public int getTuples() {
        return tuples;
    }

    public String getCSVLine() {
        String naiveCostString;
        if (plannerName.equals("naive")) {
            naiveCostString = String.format("%.4f", 1.0);
        } else {
            naiveCostString = (naiveCost != null ? String.format("%.4f", (double) naiveCost / (double) plan.toSpeechText(true).length()) : "nil");
        }
        return relation +
                "," + plannerName +
                "," + getExecutionCount() +
                "," + timedOut +
                "," + plan.toSpeechText(true).length() +
                "," + (audioSpeechCost != null ? audioSpeechCost : "nil") +
                "," + naiveCostString +
                "," + columns +
                "," + tuples +
                "," + String.format("%.5f", entropy) +
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
                ",speech cost (seconds)" +
                ",speech cost relative to naive" +
                ",number columns" +
                ",number tuples" +
                ",entropy" +
                ",mS" +
                ",mW" +
                ",mC";
    }

    public String getFileNameBase() {
        int mWIntegerValue = (int) mW;
        int tenths = (int) ((mW - mWIntegerValue) * 10);
        String mWString = mWIntegerValue + (tenths != 0 ? "point" + tenths : "");
        return relation + "_" + columns + "cols" + "_" + tuples + "tuples" + "__mS_" + mS + "__mW_" + mWString + "__mC_" + mC + "__" + plannerName;
    }

    public PlanningResult withExecutionTime(long totalExecutionTimeMillis, int executionCount) {
        this.totalExecutionTimeMillis = totalExecutionTimeMillis;
        this.executionCount = executionCount;
        return this;
    }

    public PlanningResult withPlanner(VoicePlanner planner) {
        mS = planner.getConfig().getMaxContextSize();
        mW = planner.getConfig().getMaxNumericalDomainWidth();
        mC = planner.getConfig().getMaxCategoricalDomainSize();
        plannerName = planner.getPlannerName();
        return this;
    }

    public PlanningResult withExecutionTime(long totalExecutionTimeMillis) {
        return withExecutionTime(totalExecutionTimeMillis, 1);
    }

    public PlanningResult withTimeout(boolean timedOut) {
        this.timedOut = timedOut;
        return this;
    }

    public PlanningResult forTuples(TupleCollection tuples) {
        this.tuples = tuples.tupleCount();
        this.entropy = tuples.entropy(mW);
        return this;
    }

    public PlanningResult fromQuery(Query query) {
        this.columns = query.getColumns();
        this.relation = query.getRelation().getName();
        return this;
    }

    public PlanningResult withCorrespondingNaive(PlanningResult naiveResult) {
        this.naiveCost = naiveResult.getPlan().toSpeechText(true).length();
        return this;
    }

    public void setAudioSpeechCost(int seconds) {
        this.audioSpeechCost = seconds;
    }
}
