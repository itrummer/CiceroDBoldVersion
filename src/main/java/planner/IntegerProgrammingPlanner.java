package planner;

import db.*;
import ilog.concert.*;
import ilog.cplex.*;
import planner.elements.Context;
import planner.elements.Scope;
import values.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class constructs VoiceOutputPlans according to the integer programming model. It specifically uses the CPLEX
 * integer programming solver to plan
 */
public class IntegerProgrammingPlanner extends VoicePlanner {
    private static int MAXIMAL_CONTEXT_SIZE = 4;
    private static int MAXIMAL_NUMERICAL_DOMAIN_WIDTH = 2;
    private static int MAXIMAL_CATEGORICAL_DOMAIN_SIZE = 2;

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

        try {
            IloCplex cplex = new IloCplex();

            IloIntVar[][] w = initialize2DCPLEXMatrix(cplex, cMax, tupleCount);
            IloIntVar[][] f = initialize2DCPLEXMatrix(cplex, cMax, attributeCount);
            IloIntVar[][][] l = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, tupleCollection.getLengthsOfNumericalAttributes());
            IloIntVar[][][] u = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, tupleCollection.getLengthsOfNumericalAttributes());
            IloIntVar[][][] d = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, tupleCollection.getLengthsOfCategoricalAttributes());
            IloIntVar[][][] e = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, tupleCollection.getLengthsOfNumericalAttributes());
            IloIntVar[][][] s = initializeFull3DCPLEXMatrix(cplex, cMax, tupleCount, attributeCount);
            IloIntVar[] g = cplex.intVarArray(cMax, 0,1);

            // Each tuple can be mapped to at most one context
            for (int t = 0; t < tupleCount; t++) {
                IloLinearIntExpr sumOfMappingsForTuple = cplex.linearIntExpr();
                for (int c = 0; c < cMax; c++) {
                    sumOfMappingsForTuple.addTerm(1, w[c][t]);
                }
                cplex.addLe(sumOfMappingsForTuple, 1);
            }

            // Each context can fix domains for at most MAXIMAL_CONTEXT_SIZE attributes
            for (int c = 0; c < f.length; c++) {
                cplex.addLe(cplex.sum(f[c]), MAXIMAL_CONTEXT_SIZE);
            }

            // We save time only if t is output in context c and if context c fixes the value for attribute a
            for (int c = 0; c < cMax; c++) {
                for (int t = 0; t < tupleCount; t++) {
                    s[c][t] = cplex.intVarArray(attributeCount, 0, 1);
                    for (int a = 0; a < attributeCount; a++) {
                        cplex.addLe(s[c][t][a], w[c][t]);
                        cplex.addLe(s[c][t][a], f[c][a]);
                    }
                }
            }

            // Context c must assign a lower and upper bound if it fixes a domain for attribute a
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    if (l[c][a].length > 0) {
                        cplex.addEq(cplex.sum(l[c][a]), f[c][a]);
                        cplex.addEq(cplex.sum(u[c][a]), f[c][a]);
                    }
                }
            }

            constrainBounds(cplex, cMax, attributeCount, l, u, tupleCollection);
            addConstraintsCategoricalDomainSize(cplex, cMax, attributeCount, d, attributeValueLists);
            addConstraintsOnlyAllowMatchingContexts(cplex, cMax, attributeCount, l, u, d, w, f, tupleCollection);

            // minimize speaking time (proportional to number of characters)
            for (int c = 0; c < cMax; c++) {
                for (int t = 0; t < tupleCount; t++) {
                    cplex.addGe(g[c], w[c][t]);
                }
            }

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
                contextOverhead.addTerm("Entries for  are:".length(), g[c]);
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

            IloLinearIntExpr negativeSavings = cplex.linearIntExpr();
            for (int a = 0; a < attributeCount; a++) {
                for (int t = 0; t < tupleCount; t++) {
                    for (int c = 0; c < cMax; c++) {
                        int cost = tupleCollection.getValueForAttributeAndTuple(a, t).toSpeechText().length() + tupleCollection.costForAttribute(a);
                        negativeSavings.addTerm(-cost, s[c][t][a]);
                    }
                }
            }

            // minimize the objective function
            cplex.addMinimize(cplex.sum(contextOverhead, contextTime, negativeSavings));
            cplex.solve();

            // 1. see which contexts are used, create an empty context for each used "slot" and
            //    add it by its slot number to the scopes map, as each context is assigned to one scope
            HashMap<Integer, Scope> scopes = new HashMap<Integer, Scope>();
            for (int c = 0; c < cMax; c++) {
                if (cplex.getValue(g[c]) > 0.5) {
                    scopes.put(c, new Scope(new Context()));
                }
            }

            // 2. iterate through categorical assignments, create categorical value assignments to the appropriate context
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    ArrayList<Value> valueList = attributeValueLists.get(a);
                    for (int v = 0; v < valueList.size(); v++) {
                        ArrayList<Value> valuesInDomain = new ArrayList<Value>();
                        if (cplex.getValue(d[c][a][v]) > 0.5) {
                            valuesInDomain.add(valueList.get(v));
                        }
                        if (valuesInDomain.size() > 0) {
                            Context context = scopes.get(c).getContext();
                            context.addCategoricalValueAssignments(tupleCollection.attributeForIndex(a), valuesInDomain);
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
                    if (cplex.getValue(w[c][t]) > 0.5) {
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

    private IloIntVar[][] initialize2DCPLEXMatrix(IloCplex cplex, int rows, int columns) throws IloException {
        IloIntVar[][] m = new IloIntVar[rows][];
        for (int r = 0; r < columns; r++) {
            m[r] = cplex.intVarArray(columns, 0, 1);
        }
        return m;
    }

    private IloIntVar[][][] initializeFull3DCPLEXMatrix(IloCplex cplex, int contextCount, int attributeCount, int tupleCount) throws IloException {
        IloIntVar[][][] m = new IloIntVar[contextCount][][];
        for (int c = 0; c < contextCount; c++) {
            m[c] = initialize2DCPLEXMatrix(cplex, attributeCount, tupleCount);
        }
        return m;
    }

    private IloIntVar[][][] initializeVariable3DCPLEXMatrix(IloCplex cplex, int contextCount, int attributeCount, int[] lengths) throws IloException {
        IloIntVar[][][] m = new IloIntVar[contextCount][attributeCount][0];
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (lengths[a] > 0) {
                    m[c][a] = cplex.intVarArray(lengths[a], 0, 1);
                }
            }
        }
        return m;
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
    private void constrainBounds(IloCplex cplex,
                                 int contextCount,
                                 int attributeCount,
                                 IloIntVar[][][] l,
                                 IloIntVar[][][] u,
                                 TupleCollection tupleCollection) throws IloException {
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (l[c][a].length > 0) {
                    IloLinearNumExpr lowerBound = cplex.linearNumExpr();
                    IloLinearNumExpr upperBound = cplex.linearNumExpr();
                    ArrayList<Value> valuesForAttribute = tupleCollection.distinctValuesForAttribute(a);
                    for (int v = 0; v < l[c][a].length; v++) {
                        Double coefficient = valuesForAttribute.get(v).linearProgrammingCoefficient();
                        lowerBound.addTerm(coefficient, l[c][a][v]);
                        upperBound.addTerm(coefficient, u[c][a][v]);
                    }
                    cplex.addLe(lowerBound, upperBound);
                    cplex.addLe(upperBound, cplex.prod(lowerBound, MAXIMAL_NUMERICAL_DOMAIN_WIDTH));
                }
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
                if (d[c][a].length > 0) {
                    cplex.addLe(cplex.sum(d[c][a]), MAXIMAL_CATEGORICAL_DOMAIN_SIZE);
                }
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
                                                         TupleCollection tupleCollection) throws IloException {
        for (int c = 0; c < contextCount; c++) {
            for (int a = 0; a < attributeCount; a++) {
                if (tupleCollection.attributeIsCategorical(a)) {
                    for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                        Value vT = tupleCollection.getValueForAttributeAndTuple(a, t);
                        int vTIndex = tupleCollection.getIndexOfDistinctValue(a, t);
                        // (1 - d(c,a,v_r)) + w(c,r) + f(c,a) <= 2
                        // ==> -d(c,a,v_r)  + w(c,r) + f(c,a) <= 1
                        // ==>                w(c,r) + f(c,a) <= 1 + d(c,a,v)
                        cplex.addLe(cplex.sum(w[c][t], f[c][a]), cplex.sum(1, d[c][a][vTIndex]));
                    }
                } else {
                    for (int t = 0; t < tupleCollection.tupleCount(); t++) {
//                        Value vT = valueMatrix[t][a];
//                        int vTIndex = indexMap.get(vT);
//                        for (int otherV = 0; otherV < valueList.size(); otherV++) {
//                            // for each of the other distinct values
//                            Value v = valueList.get(otherV);
//                            if (v.compareTo(vT) > 0) {
//                                // v > vT
//                                // l(c,a,v) + w(c,r) + f(c,a) <= 2
//                                cplex.addLe(cplex.sum(l[c][a][otherV], w[c][t], f[c][a]), 2);
//                            } else if (v.compareTo(vT) < 0) {
//                                // v < VT
//                                // u(c,a,v) + w(c,r) + f(c,a) <= 2
//                                cplex.addLe(cplex.sum(u[c][a][otherV], w[c][t], f[c][a]), 2);
//                            }
//                        }
                    }
                }
            }
        }
    }

}
