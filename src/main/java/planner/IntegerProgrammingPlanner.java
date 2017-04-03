package planner;

import db.TupleCollection;

/**
 * This class constructs VoiceOutputPlans according to the integer programming model. It specifically uses the CPLEX
 * integer programming solver to plan
 */
public class IntegerProgrammingPlanner extends VoicePlanner {

    /**
     * Constructs a VoiceOutputPlan using the CPLEX integer programming solver.
     * @param tupleCollection The collection of Tuples to construct a voice plan
     * @return The optimal VoiceOutputPlan according to the integer programming approach
     */
    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
//        TODO: initialize CPLEX instance and set constraints
        return null;
    }
}
