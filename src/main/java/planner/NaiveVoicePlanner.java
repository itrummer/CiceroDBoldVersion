package planner;

import db.Tuple;
import db.TupleCollection;

import java.util.ArrayList;

/**
 * A naive implementation of a voice plan. Lists all results in a query as individual tuples.
 */
public class NaiveVoicePlanner extends VoicePlanner {

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        VoiceOutputPlan outputPlan = new VoiceOutputPlan();
        outputPlan.addScope(new Scope(tupleCollection.getRows()));
        return outputPlan;
    }

}
