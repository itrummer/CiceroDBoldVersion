package planning;

import planning.config.Config;
import planning.elements.TupleCollection;

/**
 * Abstract representation of VoicePlanners
 */
public abstract class VoicePlanner {
    protected abstract VoiceOutputPlan plan(TupleCollection tupleCollection, Config config);
    public abstract String getPlannerIdentifier();
}
