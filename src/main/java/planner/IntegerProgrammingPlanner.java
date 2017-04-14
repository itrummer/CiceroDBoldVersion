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
    private static int MAXIMAL_CONTEXT_SIZE = 3;
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
        if (tupleCollection.getTuples().isEmpty()) {
            // TODO: EmptyResultVoiceOutputPlan ?
            return null;
        }

        try {
            IloCplex cplex = new IloCplex();

            // INITIALIZE INTEGER PROGRAMMING VARIABLE MATRICES
            int[] categoricalLengths = tupleCollection.getLengthsOfCategoricalAttributes();
            int[] numericalLengths = tupleCollection.getLengthsOfNumericalAttributes();

            IloIntVar[][] w = initialize2DCPLEXMatrix(cplex, cMax, tupleCount);
            IloIntVar[][] f = initialize2DCPLEXMatrix(cplex, cMax, attributeCount);
            IloIntVar[][][] l = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, numericalLengths);
            IloIntVar[][][] u = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, numericalLengths);
            IloIntVar[][][] d = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, categoricalLengths);
            IloIntVar[][][] e = initializeVariable3DCPLEXMatrix(cplex, cMax, attributeCount, numericalLengths);
            IloIntVar[][][] s = initializeFull3DCPLEXMatrix(cplex, cMax, tupleCount, attributeCount);
            IloIntVar[] g = cplex.intVarArray(cMax, 0,1);

            // ADD CONSTRAINTS TO MODEL

            // each tuple can be mapped to at most one context
            for (int t = 0; t < tupleCount; t++) {
                IloLinearIntExpr sumOfMappingsForTuple = cplex.linearIntExpr();
                for (int c = 0; c < cMax; c++) {
                    sumOfMappingsForTuple.addTerm(1, w[c][t]);
                }
                cplex.addLe(sumOfMappingsForTuple, 1);
            }

            // each context can fix domains for at most MAXIMAL_CONTEXT_SIZE attributes
            for (int c = 0; c < f.length; c++) {
                cplex.addLe(cplex.sum(f[c]), MAXIMAL_CONTEXT_SIZE);
            }

            // we save time only if t is output in context c and if context c fixes the value for attribute a
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

            // each context can fix at most MAXIMAL_CATEGORICAL_DOMAIN_SIZE values for categorical attributes
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    if (d[c][a].length > 0) {
                        cplex.addLe(cplex.sum(d[c][a]), MAXIMAL_CATEGORICAL_DOMAIN_SIZE);
                    }
                }
            }

            // numerical upper bounds must be within a certain tolerance from the lower bound value
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    if (l[c][a].length > 0) {
                        IloLinearNumExpr lowerBounds = cplex.linearNumExpr();
                        IloLinearNumExpr upperBounds = cplex.linearNumExpr();
                        for (int v = 0; v < l[c][a].length; v++) {
                            Double coefficient = tupleCollection.getDistinctValue(a, v).linearProgrammingCoefficient();
                            lowerBounds.addTerm(coefficient, l[c][a][v]);
                            upperBounds.addTerm(coefficient, u[c][a][v]);
                        }
                        cplex.addLe(lowerBounds, upperBounds);
                        cplex.addLe(upperBounds, cplex.prod(lowerBounds, MAXIMAL_NUMERICAL_DOMAIN_WIDTH));
                    }
                }
            }

            // only allow matching contexts
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    if (tupleCollection.attributeIsCategorical(a)) {
                        for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                            Value vT = tupleCollection.getValueForAttributeAndTuple(a, t);
                            int vTIndex = tupleCollection.getIndexOfDistinctValue(a, t);
                            // only one of the following terms can hold:
                            // (1 - d(c,a,v_t)) + w(c,t) + f(c,a) <= 2
                            cplex.addLe(cplex.sum(w[c][t], f[c][a]), cplex.sum(1, d[c][a][vTIndex]));
                        }
                    } else {
                        for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                            Value vT = tupleCollection.getValueForAttributeAndTuple(a, t);
                            int vTIndex = tupleCollection.getIndexOfDistinctValue(a, t);

                            for (int v = 0; v < tupleCollection.distinctValueCountForAttribute(a); v++) {
                                if (v != vTIndex) {
                                    if (tupleCollection.getDistinctValue(a, v).compareTo(vT) > 0) {
                                        // v > vT
                                        // constraint: l(c,a,v) + w(c,r) + f(c,a) <= 2
                                        cplex.addLe(cplex.sum(l[c][a][v], w[c][t], f[c][a]), 2);
                                    } else if (tupleCollection.getDistinctValue(a, v).compareTo(vT) < 0) {
                                        // v < vT
                                        // constraint: u(c,a,v) + w(c,r) + f(c,a) <= 2
                                        cplex.addLe(cplex.sum(u[c][a][v], w[c][t], f[c][a]), 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // g[c] is 1 (i.e. context c is used) iff at least one tuple is matched to it
            for (int c = 0; c < cMax; c++) {
                for (int t = 0; t < tupleCount; t++) {
                    cplex.addGe(g[c], w[c][t]);
                }
            }

            // e[c][a][v] is 1 iff both bounds are equal
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    for (int v = 0; v < e[c][a].length; v++) {
                        cplex.addLe(e[c][a][v], l[c][a][v]);
                        cplex.addLe(e[c][a][v], u[c][a][v]);
                    }
                }
            }

            // ADD COST OBJECTIVE TO MODEL

            IloIntExpr contextOverhead = cplex.prod("Entries for  are: ".length(), cplex.sum(g));

            IloLinearIntExpr contextTime = cplex.linearIntExpr();
            for (int c = 0; c < cMax; c++) {
                for (int a = 0; a < attributeCount; a++) {
                    // 1. add the cost of speaking the attribute
                    contextTime.addTerm(f[c][a], tupleCollection.attributeForIndex(a).length());

                    for (int v = 0; v < tupleCollection.distinctValueCountForAttribute(a); v++) {
                        int valueCost = tupleCollection.getDistinctValue(a, v).toSpeechText().length();
                        if (tupleCollection.attributeIsCategorical(a)) {
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

            // EXTRACT SOLUTION AS A VoiceOutputPlan

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
                    for (int v = 0; v < categoricalLengths[a]; v++) {
                        ArrayList<Value> valuesInDomain = new ArrayList<Value>();
                        if (cplex.getValue(d[c][a][v]) > 0.5) {
                            valuesInDomain.add(tupleCollection.getDistinctValue(a, v));
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
                    Value lowerBound = null;
                    Value upperBound = null;
                    for (int v = 0; v < numericalLengths[a]; v++) {
                        if (cplex.getValue(l[c][a][v]) > 0.5) {
                            lowerBound = tupleCollection.getDistinctValue(a, v);
                        } if (cplex.getValue(u[c][a][v]) > 0.5) {
                            upperBound = tupleCollection.getDistinctValue(a, v);
                        }
                    }
                    if (lowerBound != null && upperBound != null) {
                        scopes.get(c).getContext().addNumericalValueAssignment(tupleCollection.attributeForIndex(a), lowerBound, upperBound);
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
        IloIntVar[][] m = new IloIntVar[rows][0];
        for (int r = 0; r < rows; r++) {
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
                m[c][a] = cplex.intVarArray(lengths[a], 0, 1);
            }
        }
        return m;
    }
}
