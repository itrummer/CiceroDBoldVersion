package planner;

import db.TupleCollection;
import ilog.concert.*;
import ilog.cplex.*;

/**
 * This class constructs VoiceOutputPlans according to the integer programming model. It specifically uses the CPLEX
 * integer programming solver to plan
 */
public class IntegerProgrammingPlanner extends VoicePlanner {

    /**
     * Constructs a VoiceOutputPlan using the CPLEX integer programming solver. Below are the
     * variables and constraints that model the plan search space.
     *
     * Let n = number of Tuples in tupleCollection
     *
     * Maximum number of context "slots."
     * cMax = floor(n/2)
     *
     * @param tupleCollection The collection of Tuples to construct a voice plan
     * @return The optimal VoiceOutputPlan according to the integer programming approach
     */
    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        try {
            IloCplex cplex = new IloCplex();
            // TODO: initialize integer programming model and solve it
        } catch (IloException e) {
            e.printStackTrace();
        }

        return null;
    }
}
