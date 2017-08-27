package planning.planners.naive;

import planning.VoiceOutputPlan;
import planning.VoicePlanner;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.elements.Scope;

/**
 * A naive implementation of a voice buildPlan. Lists all results in a query as individual tuples.
 */
public class NaiveVoicePlanner extends VoicePlanner {

    public NaiveVoicePlanner() {

    }

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection, Config config) {
        VoiceOutputPlan outputPlan = new VoiceOutputPlan();
        outputPlan.addScope(new Scope(null, tupleCollection.getTuples(), tupleCollection.getTuplesClassName()));
        return outputPlan;
    }

    @Override
    public String getPlannerIdentifier() {
        return "naive";
    }
}
