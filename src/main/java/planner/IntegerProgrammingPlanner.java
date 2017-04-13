package planner;

import db.*;
import db.ValueAssignment;
import ilog.concert.*;
import ilog.cplex.*;
import planner.elements.Context;
import planner.elements.Scope;
import values.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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
        int attributeCount = tupleCollection.attributeCount();
        int cMax = tupleCount/2;
        ArrayList<ArrayList<Value>> attributeValueLists = tupleCollection.getAttributeValueLists();
        if (attributeValueLists.isEmpty()) {
            return null;
        }
        // valueMatrix[t][a] contains the tuple t's value for attribute a
        Value[][] valueMatrix = tupleCollection.getValueArraysForTuples();

        try {
            IloCplex cplex = new IloCplex();

            // integer programming variable matrices
            IloIntVar[][] w = initializeTupleContextMappingConstraints(cplex, cMax, tupleCount);
            IloIntVar[][] f = initializeContextAttributeDomainConstraints(cplex, cMax, attributeCount);
            IloIntVar[][][] l = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, attributeCount, attributeValueLists);
            IloIntVar[][][] u = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, attributeCount, attributeValueLists);
            IloIntVar[][][] d = initializeCategoricalAssignmentVariables(cplex, cMax, attributeCount, attributeValueLists);
//            IloIntVar[][][] s = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, attributeCount, attributeValueLists); // TODO: not correct
            IloIntVar[][][] s = new IloIntVar[cMax][tupleCount][]; // whether we save the time for outputting the value of the attribute by the use of context slot c
            for (int c = 0; c < cMax; c++) {
                for (int t = 0; t < tupleCount; t++) {
                    s[c][t] = cplex.intVarArray(attributeCount, 0, 1);
                    for (int a = 0; a < attributeCount; a++) {
                        // saving time is only possible if the following constraints hold:
                        cplex.addLe(s[c][t][a], w[c][t]); // t is output in context c in the first place
                        cplex.addLe(s[c][t][a], f[c][a]); // context c fixes the value for attribute a
                    }
                }
            }

            addContraintsContextsMustAddLowerAndUpperBound(cplex, cMax, attributeCount, l, u, f);
            addConstraintsLowerBoundsLessThanUpperBound(cplex, cMax, attributeCount, l, u, attributeValueLists);
            addConstraintsUpperBoundWithinAllowedRange(cplex, cMax, attributeCount, l, u, attributeValueLists);
            addConstraintsCategoricalDomainSize(cplex, cMax, attributeCount, d, attributeValueLists);
            addConstraintsOnlyAllowMatchingContexts(cplex, cMax, attributeCount, l, u, d, w, f, tupleCollection, valueMatrix, attributeValueLists);

            // TODO: create cost expression

            // minimize speaking time (proportional to number of characters)
            IloIntVar[] g = cplex.intVarArray(cMax, 0,1);
            for (int c = 0; c < cMax; c++) {
                for (int t = 0; t < tupleCount; t++) {
                    cplex.addGe(g[c], w[c][t]);
                }
            }

            IloIntVar[][][] e = initializeLowerOrUpperBoundVariableMatrix(cplex, cMax, attributeCount, attributeValueLists);
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    for (int v = 0; v < e[c][a].length; v++) {
                        cplex.addLe(e[c][a][v], l[c][a][v]);
                        cplex.addLe(e[c][a][v], u[c][a][v]);
                    }
                }
            }

            IloLinearIntExpr contextOverhead = cplex.linearIntExpr();
            for (int c = 0; c < cMax; c++) {
                contextOverhead.addTerm("Entries for are:".length(), g[c]);
            }

            IloLinearIntExpr contextTime = cplex.linearIntExpr();
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    // 1. add the cost of speaking the attribute
                    contextTime.addTerm(f[c][a], tupleCollection.attributeForIndex(a).length());

                    ArrayList<Value> valueList = attributeValueLists.get(a);
                    for (int v = 0; v < valueList.size(); v++) {
                        int valueCost = valueList.get(v).toSpeechText().length();
                        if (valueList.get(0).isCategorical()) {
                            // 2. add the cost of all fixed values for attribute a
                            contextTime.addTerm(d[c][a][v], valueCost);
                        } else {
                            // 2. add the cost of outputting the lower and upper bound or just lower bound if both bounds are equal
                            contextTime.addTerm(l[c][a][v], valueCost);
                            contextTime.addTerm(u[c][a][v], valueCost);
                            contextTime.addTerm(e[c][a][v], -valueCost);
                        }
                    }
                }
            }

            // cost for remaining tuples
            // iterate over all cells (2D)
            IloLinearIntExpr costOfRemainingTuples = cplex.linearIntExpr();
            for (int a = 0; a < attributeCount; a++) {
                for (int t = 0; t < tupleCount; t++) {
                    // TODO: use s to figure out if the cell is fixed by the context
//                    IloLinearIntExpr oneMinusS = cplex.linearIntExpr();
//                    oneMinusS.setConstant(1);
//                    oneMinusS.addTerm(-1, s[c][t][a]);
                }
            }

            cplex.sum(contextOverhead, contextTime, costOfRemainingTuples);

            cplex.solve();

            // 1. see which contexts are used, create an empty context for each used "slot" and
            //    add it by its slot number to the scopes map, as each context is assigned to one scope
            HashMap<Integer, Scope> scopes = new HashMap<Integer, Scope>();
            for (int c = 0; c < cMax; c++) {
                if (cplex.getValue(g[c]) == 1) {
                    scopes.put(c, new Scope(new Context()));
                }
            }

            // 2. iterate through categorical assignments, create categorical value assignments to the appropriate context
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    ArrayList<Value> valueList = attributeValueLists.get(a);
                    for (int v = 0; v < valueList.size(); v++) {
                        ArrayList<CategoricalValue> valuesInDomain = new ArrayList<CategoricalValue>();
                        if (cplex.getValue(d[c][a][v]) == 1) {
                            valuesInDomain.add((CategoricalValue) valueList.get(v));
                        }
                        if (valuesInDomain.size() > 0) {
                            Context context = scopes.get(c).getContext();
                            context.addCategoricalValueAssignment(new CategoricalValueAssignment(tupleCollection.attributeForIndex(a), valuesInDomain));
                        }
                    }
                }
            }

            // 3. iterate through lower and upper bounds, create numerical value assignments to the appropriate context
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    ArrayList<Value> valueList = attributeValueLists.get(a);
                    for (int v = 0; v < valueList.size(); v++) {
                        // TODO
                    }
                }
            }

            // 4. iterate through all tuples and add them to the matching context within a scope, or add them to the
            //    scope with an empty context if they are not assigned to any context
            Scope emptyContextScope = new Scope();
            for (int t = 0; t < tupleCount; t++) {
                boolean matched = false;
                for (int c = 0; c < cMax; c++) {
                    if (cplex.getValue(w[c][t]) == 1) {
                        matched = true;
                        scopes.get(c).addMatchingTuple(tupleCollection.getTuples().get(t));
                    }
                }
                if (!matched) {
                    emptyContextScope.addMatchingTuple(tupleCollection.getTuples().get(t));
                }
            }

            ArrayList<Scope> scopeList = new ArrayList<Scope>();
            for (Scope scope : scopes.values()) {
                scopeList.add(scope);
            }

            // add the empty context scope at the end
            scopeList.add(emptyContextScope);

            // 5. add all scopes to a VoiceOutputPlan
            return new VoiceOutputPlan(scopeList);

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
     * @return The 3D array containing the initialized integer programming variables. Object[c][a][v] is 1 if
     * context c assigns value v as the lower or upper bound of attribute a, else it is 0.
     * @throws IloException
     */
    private IloIntVar[][][] initializeLowerOrUpperBoundVariableMatrix(IloCplex cplex,
                                                                      int contextCount,
                                                                      int attributeCount,
                                                                      ArrayList<ArrayList<Value>> attributeValueLists) throws IloException {
        IloIntVar[][][] matrix = new IloIntVar[contextCount][attributeCount][0];
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                ArrayList<Value> valuesForAttribute = attributeValueLists.get(a);
                if (valuesForAttribute.get(0).isNumerical()) {
                    // only create lower or upper bound variables for numerical attributes
                    // if this attribute is categorical, it will be left as a 0 length array in matrix
                    matrix[c][a] = cplex.intVarArray(valuesForAttribute.size(), 0, 1);
                }
            }
        }
        return matrix;
    }

    // d(c,a,v) : 1 if value v is within the value domain that context c assigns to attribute a, else 0
    private IloIntVar[][][] initializeCategoricalAssignmentVariables(IloCplex cplex,
                                                                     int contextCount,
                                                                     int attributeCount,
                                                                     ArrayList<ArrayList<Value>> attributeValueLists) throws IloException {
        IloIntVar[][][] d = new IloIntVar[contextCount][attributeCount][0];
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                ArrayList<Value> valuesForAttribute = attributeValueLists.get(a);
                if (valuesForAttribute.get(0).isCategorical()) {
                    // only create d variables for categorical attributes, else leave as a 0-length array
                    d[c][a] = cplex.intVarArray(valuesForAttribute.size(), 0, 1);
                }
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
                                                                int contextCount,
                                                                int attributeCount,
                                                                IloIntVar[][][] lowerBoundVars,
                                                                IloIntVar[][][] upperBoundVars,
                                                                IloIntVar[][] contextAttributeAssignments) throws IloException {
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (lowerBoundVars[c][a].length == 0) {
                    // this means that a is not a numerical attribute, thus we
                    // do not need to create this constraint expression for a
                    continue;
                }
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
     * @param l The 3D matrix of lower bound integer programming variables. lowerBoundVars[c][a][v] contains
     *                       lower bound variable for the context c, attribute a, and value v combination
     * @param u The 3D matrix of upper bound integer programming variables. upperBoundVars[c][a][v] contains
     *                       upper bound variable for the context c, attribute a, and value v combination
     * @throws IloException
     */
    private void addConstraintsLowerBoundsLessThanUpperBound(IloCplex cplex,
                                                             int contextCount,
                                                             int attributeCount,
                                                             IloIntVar[][][] l,
                                                             IloIntVar[][][] u,
                                                             ArrayList<ArrayList<Value>> attributeValueLists) throws IloException {
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (l[c][a].length == 0) {
                    // a is categorical, no corresponding lower/upper bound
                    continue;
                }
                IloLinearNumExpr sumOfLowerMinusUpper = cplex.linearNumExpr();
                ArrayList<Value> valuesForAttribute = attributeValueLists.get(a);
                for (int v = 0; v < l[c][a].length; v++) {
                    Double coefficient = ((NumericalValue) valuesForAttribute.get(v)).getLinearProgrammingCoefficient();
                    sumOfLowerMinusUpper.addTerm(coefficient, l[c][a][v]);
                    sumOfLowerMinusUpper.addTerm(-coefficient, u[c][a][v]);
                }
                cplex.addLe(sumOfLowerMinusUpper, 0);
            }
        }
    }

    // For numerical attributes, the upper bound must be less than the maximum allowed upper bound for a given lower bound
    private void addConstraintsUpperBoundWithinAllowedRange(IloCplex cplex,
                                                            int contextCount,
                                                            int attributeCount,
                                                            IloIntVar[][][] l,
                                                            IloIntVar[][][] u,
                                                            ArrayList<ArrayList<Value>> attributeValueLists) throws IloException {
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (l[c][a].length == 0) {
                    // a is categorical
                    continue;
                }
                IloLinearNumExpr sumOfMaxAllowableUpperBoundMinusUpperBound = cplex.linearNumExpr();
                for (int v = 0; v < l[c][a].length; v++) {
                    ArrayList<Value> valuesForAttribute = attributeValueLists.get(a);
                    Double coefficient = ((NumericalValue) valuesForAttribute.get(v)).getLinearProgrammingCoefficient();
                    sumOfMaxAllowableUpperBoundMinusUpperBound.addTerm(MAXIMAL_NUMERICAL_DOMAIN_WIDTH, l[c][a][v]);
                    sumOfMaxAllowableUpperBoundMinusUpperBound.addTerm(-coefficient, u[c][a][v]);
                }
                cplex.addGe(sumOfMaxAllowableUpperBoundMinusUpperBound, 0);
            }
        }
    }

    // Constraint: Each context c can assign at most mC values for each categorical attribute a
    private void addConstraintsCategoricalDomainSize(IloCplex cplex,
                                                     int contextCount,
                                                     int attributeCount,
                                                     IloIntVar[][][] d,
                                                     ArrayList<ArrayList<Value>> attributeValueLists) throws IloException {
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (d[c][a].length == 0) {
                    // a is numerical
                    continue;
                }
                IloLinearIntExpr sumOfCategoricalAssignments = cplex.linearIntExpr();
                for (int v = 0; v < d[c][a].length; v++) {
                    sumOfCategoricalAssignments.addTerm(1, d[c][a][v]);
                }
                cplex.addLe(sumOfCategoricalAssignments, MAXIMAL_CATEGORICAL_DOMAIN_SIZE);
            }
        }
    }

    /**
     * Adds constraints such that tuples are only matched to "matching" contexts. Deals with numerical and categorical
     * attributes in separate ways.
     */
    private void addConstraintsOnlyAllowMatchingContexts(IloCplex cplex,
                                                         int contextCount,
                                                         int attributeCount,
                                                         IloIntVar[][][] l,
                                                         IloIntVar[][][] u,
                                                         IloIntVar[][][] d,
                                                         IloIntVar[][] w,
                                                         IloIntVar[][] f,
                                                         TupleCollection tupleCollection,
                                                         Value[][] valueMatrix,
                                                         ArrayList<ArrayList<Value>> attributeValueLists) throws IloException {
        // TODO: This is very messy, but works for now. Will clean up later...
        ArrayList<HashMap<Value, Integer>> distinctValueIndexForAttribute = tupleCollection.listOfIndicesForValues();
        for (int a = 0; a < attributeCount; a++) {
            ArrayList<Value> valueList = attributeValueLists.get(a);
            HashMap<Value, Integer> indexMap = distinctValueIndexForAttribute.get(a);
            for (int c = 0; c < contextCount; c++) {
                if (valueList.get(0).isCategorical()) {
                    for (int t = 0; t < valueMatrix.length; t++) {
                        // for each context c and tuple t combination, we find which distinct value index
                        // to use to extract the corresponding IloIntVar
                        Value vT = valueMatrix[t][a];
                        int vTIndex = indexMap.get(vT);
                        // (1 - d(c,a,v_r)) + w(c,r) + f(c,a) <= 2
                        // ==> -d(c,a,v,v_r + w(c,r) + f(c,a) <= 1
                        IloLinearIntExpr firstTerm = cplex.linearIntExpr();
                        firstTerm.addTerm(-1, d[c][a][vTIndex]);
                        cplex.le(cplex.sum(firstTerm, w[c][t], f[c][a]), 1);
                    }
                } else {
                    for (int t = 0; t < valueMatrix.length; t++) {
                        NumericalValue vT = (NumericalValue) valueMatrix[t][a];
                        int vTIndex = indexMap.get(vT);
                        for (int otherV = 0; otherV < valueList.size(); otherV++) {
                            // for each of the other distinct values
                            NumericalValue v = (NumericalValue) valueList.get(otherV);
                            if (v.compareTo(vT) > 0) {
                                // v > vT
                                // l(c,a,v) + w(c,r) + f(c,a) <= 2
                                cplex.addLe(cplex.sum(l[c][a][otherV], w[c][t], f[c][a]), 2);
                            } else if (v.compareTo(vT) < 0) {
                                // v < VT
                                // u(c,a,v) + w(c,r) + f(c,a) <= 2
                                cplex.addLe(cplex.sum(u[c][a][otherV], w[c][t], f[c][a]), 2);
                            }
                        }
                    }
                }
            }
        }
    }

}
