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
    public abstract VoiceOutputPlan plan(TupleCollection tupleCollection);

    public ToleranceConfig getConfig() {
        return new ToleranceConfig();
    }

    public abstract String getPlannerName();

}
