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
     * Inputs:
     *   n : number of Tuples in tupleCollection
     *   cMax := n / 2; maximum number of contexts
     *   mS : maximal context size
     *   mC : maximum number of values a context can assign to a domain for an attribute
     *   mW : maximum width of an assigned value domain for a numerical attribute; config parameter
     *
     * Constraints:
     *   Each row can be mapped to at most one context
     *      sum(c) w(r,c) <= 1 for all rows r
     *
     *   Each context can contain at most mS contexts
     *      sum(a) f(c,a) <= mS for all contexts c
     *
     *   Each context c can assign at most mC values for each categorical attribute a
     *      sum(v) d(c,a,v) <= mC for each context c and attribute a
     *
     *   Each context must assign a lower and upper bound for all numerical attributes it assigns a value domain to
     *      sum(v) l(c,a,v) <= f(c,a)
     *      sum(v) u(c,a,v) <= f(c,a)
     *
     *   Lower bounds must be below upper bound
     *      sum(v) v * l(c,a,v) <= sum(v) u(c,a,v)
     *
     *   For numerical attributes, the upper bound must be less than the maximum allowed upper bound for a given lower bound
     *      sum(v) v * u(c,a,v) <= mW * sum(v) l(c,a,v)
     *
     *   Only allow rows to be assigned to "matching" contexts
     *      let v_r : value in row r for attribute a
     *      if a is numerical:
     *          for each c and v > v_r:
     *              l(c,a,v) + w(c,r) + f(c,a) <= 2
     *          for each c and v < v_r:
     *              u(c,a,v) + w(c,r) + f(c,a) <= 2
     *      if a is categorical:
     *          for each c:
     *              (1 - d(c,a,v_r)) + w(c,r) + f(c,a) <= 2
     *
     * @param tupleCollection The collection of Tuples to construct a voice plan
     * @return The optimal VoiceOutputPlan according to the integer programming approach
     */
    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {

        int numRows = tupleCollection.getRows().size();
        int numAttributes = tupleCollection.getAttributeCount();
        int cMax = numRows / 2;

        // TODO: replace valueCount array with data from tupleCollection
        // uniqueValueCount represents the permissible values that appear in the tupleCollection
        int[] uniqueValueCount = new int[numAttributes];
        for (int i = 0; i < uniqueValueCount.length; i++) {
            uniqueValueCount[i] = 8;
        }

        try {
            IloCplex cplex = new IloCplex();

            // w(c,r) : 1 if row r is mapped to context c, else 0
            IloIntVar[][] w = new IloIntVar[cMax][];
            for (int c = 0; c < w.length; c++) {
                w[c] = cplex.intVarArray(numRows, 0, 1);
            }

            // f(c,a) : 1 if context c contains a domain mapping for attribute a, else 0
            IloIntVar[][] f = new IloIntVar[cMax][];
            for (int c = 0; c < w.length; c++) {
                f[c] = cplex.intVarArray(numAttributes, 0, 1);
            }

            // l(c,a,v), u(c,a,v) : 1 if context c assigns value v as the lower or upper bound for attribute a, else 0
            IloNumVar[][][] l = new IloNumVar[cMax][numAttributes][];
            IloNumVar[][][] u = new IloNumVar[cMax][numAttributes][];
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numAttributes; a++) {
                    l[c][a] = cplex.intVarArray(uniqueValueCount[a], 0, 1);
                    u[c][a] = cplex.intVarArray(uniqueValueCount[a], 0, 1);
                }
            }

            // d(c,a,v) : 1 if value v is within the value domain that context c assigns to attribute a, else 0
            IloNumVar[][][] d = new IloNumVar[cMax][numAttributes][];
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numAttributes; a++) {
                    d[c][a] = cplex.intVarArray(uniqueValueCount[a], 0, 1);
                }
            }

        } catch (IloException e) {
            e.printStackTrace();
        }

        return null;
    }

}
