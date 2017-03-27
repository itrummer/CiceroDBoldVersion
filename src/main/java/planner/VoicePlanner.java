package planner;

import db.Tuple;
import db.TupleCollection;

/**
 * Abstract representation of VoicePlanners
 */
public abstract class VoicePlanner {

    /**
     * Visits a TupleCollection to construct a VoiceOutputPlan that represents the contents of rowCollection
     * @param tupleCollection The data to represent in speech
     */
    public abstract void plan(TupleCollection tupleCollection);

    /**
     * Visits a Tuple and includes it in the VoiceOutputPlan
     * @param tuple The row to be included in the voice plan
     */
    public abstract void plan(Tuple tuple);

}
