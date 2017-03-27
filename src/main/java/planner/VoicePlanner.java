package planner;

import db.Row;
import db.RowCollection;

/**
 * Abstract representation of VoicePlanners
 */
public abstract class VoicePlanner {

    /**
     * Visits a RowCollection to construct a VoicePlan that represents the contents of rowCollection
     * @param rowCollection The data to represent in speech
     */
    public abstract void plan(RowCollection rowCollection);

    /**
     * Visits a Row and includes it in the VoicePlan
     * @param row The row to be included in the voice plan
     */
    public abstract void plan(Row row);

}
