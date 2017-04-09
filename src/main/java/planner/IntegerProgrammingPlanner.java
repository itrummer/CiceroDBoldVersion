package planner;

import db.TupleCollection;
import ilog.concert.*;
import ilog.cplex.*;

/**
 * This class constructs VoiceOutputPlans according to the integer programming model. It specifically uses the CPLEX
 * integer programming solver to plan
 */
public class IntegerProgrammingPlanner extends VoicePlanner {
    // mS : maximum number of attribute mappings per context
    static int MAXIMAL_CONTEXT_SIZE = 4;
    // mW : maximum allowable size for a numerical attribute domain
    static int MAXIMAL_NUMERICAL_DOMAIN_WIDTH = 5;
    // mC : maximum allowable size for a categorical attribute domain
    static int MAXIMAL_CATEGORICAL_DOMAIN_SIZE = 3;

    /**
     * Constructs a VoiceOutputPlan using the CPLEX integer programming solver.
     *
     * Constraints TODO:
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
        int numRows = tupleCollection.getTuples().size();
        int numAttributes = tupleCollection.getAttributeCount();
        int cMax = numRows/2;

        Object[][] categoricalValues = tupleCollection.getValueMatrix(true);
        Object[][] numericalValues = tupleCollection.getValueMatrix(false);

        try {
            IloCplex cplex = new IloCplex();

            // w(c,r) : 1 if row r is mapped to context c, else 0
            IloIntVar[][] w = new IloIntVar[cMax][];
            for (int c = 0; c < w.length; c++) {
                w[c] = cplex.intVarArray(numRows, 0, 1);
            }

            // Constraint : each row r can be mapped to at most 1 context
            for (int r = 0; r < numRows; r++) {
                IloLinearIntExpr sumOfMappingsForRow = cplex.linearIntExpr();
                for (int c = 0; c < cMax; c++) {
                    sumOfMappingsForRow.addTerm(1, w[c][r]);
                }
                cplex.addLe(sumOfMappingsForRow, 1);
            }

            // f(c,a) : 1 if context c contains a domain mapping for attribute a, else 0
            IloIntVar[][] f = new IloIntVar[cMax][];
            for (int c = 0; c < w.length; c++) {
                f[c] = cplex.intVarArray(numAttributes, 0, 1);
            }

            // Constraint: Each context can contain at most mS domain mappings
            for (int c = 0; c < cMax; c++) {
                IloLinearIntExpr sumOfMappingsForContext = cplex.linearIntExpr();
                for (int a = 0; a < f[c].length; a++) {
                    sumOfMappingsForContext.addTerm(1, f[c][a]);
                }
                cplex.addLe(sumOfMappingsForContext, MAXIMAL_CONTEXT_SIZE);
            }

            // l(c,a,v), u(c,a,v) : 1 if context c assigns value v as the lower or upper bound for attribute a, else 0
            IloIntVar[][][] l = new IloIntVar[cMax][numericalValues.length][];
            IloIntVar[][][] u = new IloIntVar[cMax][numericalValues.length][];
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numAttributes; a++) {
                    l[c][a] = cplex.intVarArray(numericalValues[a].length, 0, 1);
                    u[c][a] = cplex.intVarArray(numericalValues[a].length, 0, 1);
                }
            }

            // Constraint: Each context must assign a lower and upper bound for all numerical attributes
            // to which it assigns a value domain
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numAttributes; a++) {
                    IloLinearIntExpr sumOfLowerBounds = cplex.linearIntExpr();
                    IloLinearIntExpr sumOfUpperBounds = cplex.linearIntExpr();
                    for (int v = 0; v < l[c][a].length; v++) {
                        sumOfLowerBounds.addTerm(1, l[c][a][v]);
                        sumOfUpperBounds.addTerm(1, u[c][a][v]);
                    }
                    sumOfLowerBounds.addTerm(-1, f[c][a]);
                    sumOfUpperBounds.addTerm(-1, f[c][a]);
                    cplex.addEq(sumOfLowerBounds, 0);
                    cplex.addEq(sumOfUpperBounds, 0);
                }
            }

            // Constraint: Lower bounds must be below upper bound
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numericalValues.length; a++) {
                    IloLinearIntExpr sumOfLowerMinusUpper = cplex.linearIntExpr();
                    for (int v = 0; v < numericalValues[a].length; v++) {
                        // TODO: replace 1 and -1 with a LinearNumVar for each value, may need to create
                        // a LinearNumVar for each distinct value
                        sumOfLowerMinusUpper.addTerm(1, l[c][a][v]);
                        sumOfLowerMinusUpper.addTerm(-1, u[c][a][v]);
                    }
                    // only one upper and lower bound set; thus lower minus upper needs to be negative
                    cplex.addLe(sumOfLowerMinusUpper, 0);
                }
            }

            // For numerical attributes, the upper bound must be less than the maximum allowed upper bound for a given lower bound
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numericalValues.length; a++) {
                    IloLinearIntExpr sumOfMaxAllowableUpperBoundMinusUpperBound = cplex.linearIntExpr();
                    for (int v = 0; v < numericalValues[a].length; v++) {
                        sumOfMaxAllowableUpperBoundMinusUpperBound.addTerm(MAXIMAL_NUMERICAL_DOMAIN_WIDTH, l[c][a][v]);
                        // TODO: replace -1 with upper bound value
                        sumOfMaxAllowableUpperBoundMinusUpperBound.addTerm(-1, u[c][a][v]);
                    }
                    cplex.addGe(sumOfMaxAllowableUpperBoundMinusUpperBound, 0);
                }
            }

            // d(c,a,v) : 1 if value v is within the value domain that context c assigns to attribute a, else 0
            IloIntVar[][][] d = new IloIntVar[cMax][categoricalValues.length][];
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < categoricalValues.length; a++) {
                    d[c][a] = cplex.intVarArray(categoricalValues[a].length, 0, 1);
                }
            }

            // Constraint: Each context c can assign at most mC values for each categorical attribute a
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < categoricalValues.length; a++) {
                    IloLinearIntExpr sumOfCategoricalAssignments = cplex.linearIntExpr();
                    for (int v = 0; v < categoricalValues[a].length; v++) {
                        sumOfCategoricalAssignments.addTerm(1, d[c][a][v]);
                    }
                    cplex.addLe(sumOfCategoricalAssignments, MAXIMAL_CATEGORICAL_DOMAIN_SIZE);
                }
            }

        } catch (IloException e) {
            e.printStackTrace();
        }

        return null;
    }

}
