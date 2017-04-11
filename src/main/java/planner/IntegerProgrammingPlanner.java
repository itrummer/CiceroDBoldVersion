package planner;

import db.TupleCollection;
import ilog.concert.*;
import ilog.cplex.*;

/**
 * This class constructs VoiceOutputPlans according to the integer programming model. It specifically uses the CPLEX
 * integer programming solver to plan
 */
public class IntegerProgrammingPlanner extends VoicePlanner {
    private static int MAXIMAL_CONTEXT_SIZE = 4;
    private static int MAXIMAL_NUMERICAL_DOMAIN_WIDTH = 5;
    private static int MAXIMAL_CATEGORICAL_DOMAIN_SIZE = 3;

    /**
     * Constructs a VoiceOutputPlan using the CPLEX integer programming solver.
     * @param tupleCollection The collection of Tuples to construct a voice plan
     * @return The optimal VoiceOutputPlan according to the integer programming approach
     */
    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        int tupleCount = tupleCollection.tupleCount();
        int cMax = tupleCount/2;
        Object[][] categoricalValueMatrix = tupleCollection.getCategoricalValueMatrix();
        Object[][] numericalValueMatrix = tupleCollection.getNumericalValueMatrix();
        int categoricalAttributeCount = numericalValueMatrix.length;
        int numericalAttributeCount = categoricalValueMatrix.length;

        try {
            IloCplex cplex = new IloCplex();
            IloIntVar[][] w = initializeTupleContextMappingConstraints(cplex, cMax, tupleCount);
            IloIntVar[][] f = initializeContextAttributeDomainConstraints(cplex, cMax, tupleCount);
            IloIntVar[][] fNumerical = initializeContextAttributeDomainConstraints(cplex, cMax, numericalAttributeCount);
            IloIntVar[][] fCategorical = initializeContextAttributeDomainConstraints(cplex, cMax, categoricalAttributeCount);
            IloIntVar[][][] l = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, numericalValueMatrix);
            IloIntVar[][][] u = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, numericalValueMatrix);
            IloIntVar[][][] d = initializeCategoricalAssignmentVariables(cplex, cMax, categoricalValueMatrix);
            IloIntVar[][][] sNumerical = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, numericalValueMatrix);
            IloIntVar[][][] sCategorical = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, categoricalValueMatrix);

            addContraintsContextsMustAddLowerAndUpperBound(cplex, l, u, f);
            addConstraintsLowerBoundsLessThanUpperBound(cplex, l, u);
            addConstraintsUpperBoundWithinAllowedRange(cplex, l, u, numericalValueMatrix);
            addConstraintsCategoricalDomainSize(cplex, d, categoricalValueMatrix);
            addConstraintsOnlyAllowMatchingContexts(cplex, l, u);

            // TODO: create cost expression

            // minimize speaking time (proportional to number of characters)
            IloIntVar[] g = cplex.intVarArray(cMax, 0,1);
            for (int c = 0; c < cMax; c++) {
                for (int t = 0; t < tupleCount; t++) {
                    cplex.addGe(g[c], w[c][t]);
                }
            }

            IloIntVar[][][] e = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, numericalValueMatrix);
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numericalValueMatrix.length; a++) {
                    for (int v = 0; v < numericalValueMatrix[a].length; v++) {
                        cplex.addLe(e[c][a][v], l[c][a][v]);
                        cplex.addLe(e[c][a][v], u[c][a][v]);
                    }
                }
            }

            IloLinearIntExpr contextOverhead = cplex.linearIntExpr();
            for (int c = 0; c < cMax; c++) {
                contextOverhead.addTerm("Entries for are:".length(), g[c]);
            }

            IloLinearIntExpr categoricalContextTime = cplex.linearIntExpr();
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < categoricalValueMatrix.length; a++) {
                    // TODO: replace with cost of actual column name
                    categoricalContextTime.addTerm(f[c][a], "column name".length());
                    for (int v = 0; v < categoricalValueMatrix[a].length; v++) {
                        // TODO: replace with actual value
                        categoricalContextTime.addTerm(d[c][a][v], "value".length());
                    }
                }
            }

            IloLinearIntExpr contextNumericalTime = cplex.linearIntExpr();
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < numericalValueMatrix.length; a++) {
                    contextNumericalTime.addTerm(f[c][a], "column name".length());
                    for (int v = 0; v < numericalValueMatrix[a].length; v++) {
                        contextNumericalTime.addTerm(l[c][a][v], "lower bound cost".length());
                        contextNumericalTime.addTerm(u[c][a][v], "upper bound cost".length());
                        contextNumericalTime.addTerm(sNumerical[c][a][v], -"upper bound cost".length());
                    }
                }
            }

            // cost for remaining tuples
            // iterate over all cells (2D)
            // use s to figure out if the cell is fixed by the context
            for (int t = 0; t < tupleCount; t++) {
                for (int a = 0; a < numericalAttributeCount; a++) {

                }
                for (int a = 0; a < categoricalAttributeCount; a++) {

                }
            }

            cplex.sum(contextOverhead, contextNumericalTime, categoricalContextTime);

            cplex.solve();

            // TODO: extract plan from solved cplex

        } catch (IloException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper method to initialize the integer variables that represent the tuple-context mappings.
     * For each tuple t, context c combination we create a variable in our CPLEX model to represent
     * the possibility that tuple t may be matched to context c. This variable will be 1 if tuple t
     * is mapped to context c, else it will be 0.
     *
     * @param cplex The CPLEX model to be used to initialize variables and to which constraints will be added
     * @param contextCount The number of contexts for which to create variables and constraints
     * @param tupleCount The number of tuples for which to create variables and constraints
     * @return A 2D matrix containing the IloIntVars initialized in the CPLEX model. Entry w[c][t] contains
     * the IloIntVar for context c and tuple t
     * @throws IloException
     */
    private IloIntVar[][] initializeTupleContextMappingConstraints(IloCplex cplex,
                                                                   int contextCount,
                                                                   int tupleCount) throws IloException {
        IloIntVar[][] w = new IloIntVar[contextCount][];
        for (int c = 0; c < w.length; c++) {
            w[c] = cplex.intVarArray(tupleCount, 0, 1);
        }

        for (int t = 0; t < tupleCount; t++) {
            IloLinearIntExpr sumOfMappingsForTuple = cplex.linearIntExpr();
            for (int c = 0; c < contextCount; c++) {
                sumOfMappingsForTuple.addTerm(1, w[c][t]);
            }
            cplex.addLe(sumOfMappingsForTuple, 1);
        }
        return w;
    }

    /**
     * Helper method to initialize the integer variables that represent the context-attribute assignments.
     * For each context c, attribute a combination we create a variable in our CPLEX model to represent the
     * possibility that context c may assign a domain of values for the attribute a. Also, we add constraints
     * that only allow contexts to have at most a certain number of domain assignments for the corresponding
     * number of attributes. This is the configuration parameter MAXIMAL_CONTEXT_SIZE.
     *
     * @param cplex The CPLEX model to be used to initialize variables and to which constraints will be added
     * @param contextCount The number of contexts for which to create variables and constraints
     * @param attributeCount The number of tuples for which to create variables and constraints
     * @return A 2D matrix containing the IloIntVars initialized in the CPLEX model. Entry f[c][a] contains
     * the IloIntVar for context c and attribute a
     * @throws IloException
     */
    private IloIntVar[][] initializeContextAttributeDomainConstraints(IloCplex cplex,
                                                                      int contextCount,
                                                                      int attributeCount) throws IloException {
        IloIntVar[][] f = new IloIntVar[contextCount][];
        for (int c = 0; c < contextCount; c++) {
            f[c] = cplex.intVarArray(attributeCount, 0, 1);
        }

        for (int c = 0; c < contextCount; c++) {
            IloLinearIntExpr sumOfMappingsForContext = cplex.linearIntExpr();
            for (int a = 0; a < f[c].length; a++) {
                sumOfMappingsForContext.addTerm(1, f[c][a]);
            }
            cplex.addLe(sumOfMappingsForContext, MAXIMAL_CONTEXT_SIZE);
        }
        return f;
    }

    /**
     * Helper method to initialize the 3D array that holds the lower or upper bound matrix. Each entry in this
     * matrix represents the possibility that context c assigns value v as the lower or upper bound for attribute a
     *
     * @param cplex The CPLEX model used to initialize variables
     * @param numericalValueMatrix The 2D matrix of numerical values. numerical[a] should contain the
     *                             the distinct value list for numerical attribute a
     * @return The 3D array containing the initialized integer programming variables. Object[c][a][v] is 1 if
     * context c assigns value v as the lower or upper bound of attribute a, else it is 0.
     * @throws IloException
     */
    private IloIntVar[][][] initializeLowerOrUpperBoundVariableMatrix(IloCplex cplex,
                                                                      int contextCount,
                                                                      Object[][] numericalValueMatrix) throws IloException {
        IloIntVar[][][] matrix = new IloIntVar[contextCount][numericalValueMatrix.length][];
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < numericalValueMatrix.length; a++) {
                matrix[c][a] = cplex.intVarArray(numericalValueMatrix[a].length, 0, 1);
            }
        }
        return matrix;
    }

    // d(c,a,v) : 1 if value v is within the value domain that context c assigns to attribute a, else 0
    private IloIntVar[][][] initializeCategoricalAssignmentVariables(IloCplex cplex,
                                                                     int contextCount,
                                                                     Object[][] categoricalValueMatrix) throws IloException {
        IloIntVar[][][] d = new IloIntVar[contextCount][categoricalValueMatrix.length][];
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < categoricalValueMatrix.length; a++) {
                d[c][a] = cplex.intVarArray(categoricalValueMatrix[a].length, 0, 1);
            }
        }
        return d;
    }

    /**
     * Adds constraints to the CPLEX instance such that a context can only assign a lower bound to an attribute
     * if and only if it applies an upper bound. We create this constraint by specifying that the total number of
     * lower or upper bounds must be equal to the number of assignments a context has in numerical attributes.
     * Equivalently, we specify that the total number of lower or upper bound assignments minus the number of
     * domain assignments for a context must be 0.
     *
     * @param cplex The CPLEX model to which constraints will be added
     * @param lowerBoundVars The 3D matrix of lower bound integer programming variables. lowerBoundVars[c][a][v] contains
     *                       lower bound variable for the context c, attribute a, and value v combination
     * @param upperBoundVars The 3D matrix of upper bound integer programming variables. upperBoundVars[c][a][v] contains
     *                       upper bound variable for the context c, attribute a, and value v combination
     * @param contextAttributeAssignments The 2D matrix containing variables that represent whether context c assigns a value
     *                                    domain for an attribute a
     */
    private void addContraintsContextsMustAddLowerAndUpperBound(IloCplex cplex,
                                                                IloIntVar[][][] lowerBoundVars,
                                                                IloIntVar[][][] upperBoundVars,
                                                                IloIntVar[][] contextAttributeAssignments) throws IloException {
        for (int c = 0; c < lowerBoundVars.length; c++) {
            for (int a = 0; a < lowerBoundVars[c].length; a++) {
                IloLinearIntExpr sumOfLowerBoundAssignments = cplex.linearIntExpr();
                IloLinearIntExpr sumOfUpperBoundAssignments = cplex.linearIntExpr();
                for (int v = 0; v < lowerBoundVars[c][a].length; v++) {
                    sumOfLowerBoundAssignments.addTerm(1, lowerBoundVars[c][a][v]);
                    sumOfUpperBoundAssignments.addTerm(1, upperBoundVars[c][a][v]);
                }
                sumOfLowerBoundAssignments.addTerm(-1, contextAttributeAssignments[c][a]);
                sumOfUpperBoundAssignments.addTerm(-1, contextAttributeAssignments[c][a]);
                cplex.addEq(sumOfLowerBoundAssignments, 0);
                cplex.addEq(sumOfUpperBoundAssignments, 0);
            }
        }
    }

    /**
     * Adds constraint that the lower bounds must be less than the upper bounds
     *
     * @param cplex The CPLEX model to which constraints will be added
     * @param lowerBoundVars The 3D matrix of lower bound integer programming variables. lowerBoundVars[c][a][v] contains
     *                       lower bound variable for the context c, attribute a, and value v combination
     * @param upperBoundVars The 3D matrix of upper bound integer programming variables. upperBoundVars[c][a][v] contains
     *                       upper bound variable for the context c, attribute a, and value v combination
     * @throws IloException
     */
    private void addConstraintsLowerBoundsLessThanUpperBound(IloCplex cplex,
                                                             IloIntVar[][][] lowerBoundVars,
                                                             IloIntVar[][][] upperBoundVars) throws IloException {
        for (int c = 0; c < lowerBoundVars.length; c++) {
            for (int a = 0; a < lowerBoundVars[c].length; a++) {
                IloLinearIntExpr sumOfLowerMinusUpper = cplex.linearIntExpr();
                for (int v = 0; v < lowerBoundVars[c][a].length; v++) {
                    // TODO: replace 1 and -1 with a LinearNumVar for each value
                    sumOfLowerMinusUpper.addTerm(1, lowerBoundVars[c][a][v]);
                    sumOfLowerMinusUpper.addTerm(-1, upperBoundVars[c][a][v]);
                }
                cplex.addLe(sumOfLowerMinusUpper, 0);
            }
        }
    }

    // For numerical attributes, the upper bound must be less than the maximum allowed upper bound for a given lower bound
    private void addConstraintsUpperBoundWithinAllowedRange(IloCplex cplex,
                                                            IloIntVar[][][] lowerBoundVars,
                                                            IloIntVar[][][] upperBoundVars,
                                                            Object[][] numericalValueMatrix) throws IloException {
        for (int c = 0; c < numericalValueMatrix.length; c++) {
            for (int a = 0; a < numericalValueMatrix.length; a++) {
                IloLinearIntExpr sumOfMaxAllowableUpperBoundMinusUpperBound = cplex.linearIntExpr();
                for (int v = 0; v < numericalValueMatrix[a].length; v++) {
                    sumOfMaxAllowableUpperBoundMinusUpperBound.addTerm(MAXIMAL_NUMERICAL_DOMAIN_WIDTH, lowerBoundVars[c][a][v]);
                    // TODO: replace -1 with upper bound value
                    sumOfMaxAllowableUpperBoundMinusUpperBound.addTerm(-1, upperBoundVars[c][a][v]);
                }
                cplex.addGe(sumOfMaxAllowableUpperBoundMinusUpperBound, 0);
            }
        }
    }

    // Constraint: Each context c can assign at most mC values for each categorical attribute a
    private void addConstraintsCategoricalDomainSize(IloCplex cplex,
                                                     IloIntVar[][][] valueAssignmentVars,
                                                     Object[][] categoricalValueMatrix) throws IloException {
        for (int c = 0; c < valueAssignmentVars.length; c++) {
            for (int a = 0; a < categoricalValueMatrix.length; a++) {
                IloLinearIntExpr sumOfCategoricalAssignments = cplex.linearIntExpr();
                for (int v = 0; v < categoricalValueMatrix[a].length; v++) {
                    sumOfCategoricalAssignments.addTerm(1, valueAssignmentVars[c][a][v]);
                }
                cplex.addLe(sumOfCategoricalAssignments, MAXIMAL_CATEGORICAL_DOMAIN_SIZE);
            }
        }
    }

    /**
     * TODO: only allow matching contexts
     * let v_r : value in row r for attribute a
     * if a is numerical:
     *   for each c and v > v_r:
     *     l(c,a,v) + w(c,r) + f(c,a) <= 2
     *   for each c and v < v_r:
     *     u(c,a,v) + w(c,r) + f(c,a) <= 2
     * if a is categorical:
     *   for each c:
     *     (1 - d(c,a,v_r)) + w(c,r) + f(c,a) <= 2
     */
    private void addConstraintsOnlyAllowMatchingContexts(IloCplex cplex,
                                                         IloIntVar[][][] lowerBoundVars,
                                                         IloIntVar[][][] upperBoundVars) {
    }

}
