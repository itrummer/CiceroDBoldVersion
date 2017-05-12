package planner.hybrid;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import planner.VoiceOutputPlan;
import planner.VoicePlanner;
import planner.elements.*;
import util.DatabaseUtilities;

import java.util.*;

/**
 */
public class HybridPlanner extends VoicePlanner {
    private int maximalContextSize;
    private double maximalNumericalDomainWidth;
    private int maximalCategoricalDomainSize;

    public HybridPlanner(int mS, double mW, int mC) {
        this.maximalContextSize = mS;
        this.maximalNumericalDomainWidth = mW;
        this.maximalCategoricalDomainSize = mC;
    }

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        ArrayList<Context> contextCandidates = generateContextCandidates(tupleCollection);

        for (Context c : contextCandidates) {
            System.out.println(c);
        }

        VoiceOutputPlan plan = null;

        try {
            IloCplex cplex = new IloCplex();

            int contextCount = contextCandidates.size();

            IloLinearNumExpr totalCost = cplex.linearNumExpr();
            IloIntVar[][] w = new IloIntVar[contextCount][];
            IloIntVar[] g = cplex.intVarArray(contextCount, 0, 1);

            for (int c = 0; c < contextCount; c++) {
                w[c] = cplex.intVarArray(tupleCollection.tupleCount(), 0, 1);
            }

            for (int c = 0; c < contextCount; c++) {
                Context context = contextCandidates.get(c);
                double contextCost = Scope.contextOverheadCost() + context.toSpeechText(true).length();
                totalCost.addTerm(contextCost, g[c]);
            }

            for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                Tuple tuple = tupleCollection.getTuples().get(t);
                int tWithoutContext = tuple.toSpeechText(true).length();
                for (int c = 0; c < contextCount; c++) {
                    Context context = contextCandidates.get(c);
                    if (context.matches(tuple)) {
                        int tWithContext = tuple.toSpeechText(context, true).length();
                        int savings = tWithoutContext - tWithContext;
                        totalCost.addTerm(-savings, w[c][t]);
                    } else {
                        cplex.addEq(w[c][t], 0);
                    }
                }
            }

            for (int c = 0; c < contextCount; c++) {
                // a context is used only if at least one tuple is output in it
                for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                    cplex.addGe(g[c], w[c][t]);
                }
            }

            // constraint: each tuple mapped to one context
            for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                IloLinearIntExpr sumForT = cplex.linearIntExpr();
                for (int c = 0; c < contextCount; c++) {
                    sumForT.addTerm(1, w[c][t]);
                }
                cplex.addLe(sumForT, 1);
            }

            cplex.addMinimize(totalCost);

            cplex.solve();

            // parse CPLEX output
            HashMap<Integer, ArrayList<Tuple>> tupleBins = new HashMap<>();
            for (int c = 0; c < contextCount; c++) {
                if (cplex.getValue(g[c]) > 0.5) {
                    tupleBins.put(c, new ArrayList<>());
                }
            }

            ArrayList<Tuple> emptyContextTuples = new ArrayList<>();

            for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                boolean isMatched = false;
                for (int c = 0; c < contextCount; c++) {
                    if (cplex.getValue(w[c][t]) > 0.5) {
                        System.out.println("Tuple " + t + " matched to context " + c);
                        tupleBins.get(c).add(tupleCollection.getTuples().get(t));
                        isMatched = true;
                    }
                }
                if (!isMatched) {
                    System.out.println("Tuple " + t + " matched to empty context");
                    emptyContextTuples.add(tupleCollection.getTuples().get(t));
                }
            }

            ArrayList<Scope> scopes = new ArrayList<>();
            if (!emptyContextTuples.isEmpty()) {
                scopes.add(new Scope(emptyContextTuples));
            }

            for (int c = 0; c < contextCandidates.size(); c++) {
                if (tupleBins.containsKey(c)) {
                    scopes.add(new Scope(contextCandidates.get(c), tupleBins.get(c)));
                }
            }
            System.out.println(scopes);

            plan = new VoiceOutputPlan(scopes);

        } catch (IloException e) {
            e.printStackTrace();
        }

        return plan;
    }

    public boolean useless(Context c, TupleCollection tupleCollection) {
        int totalSavings = 0;
        for (Tuple t : tupleCollection) {
            if (c.matches(t)) {
                int tWithContext = t.toSpeechText(c, true).length();
                int tWithoutContext = t.toSpeechText(true).length();
                totalSavings += tWithoutContext - tWithContext;
            }
        }
        return c.toSpeechText(true).length() + Scope.contextOverheadCost() >= totalSavings;
    }

    public ArrayList<Context> generateContextCandidates(TupleCollection tupleCollection) {
        Map<Integer, Set<ValueDomain>> candidateAssignments = tupleCollection.candidateAssignments(maximalCategoricalDomainSize, maximalNumericalDomainWidth);

        ArrayList<Context> result = new ArrayList<>();

        int k = 0;
        ArrayList<Context> kAssignmentContexts = new ArrayList<>();
        kAssignmentContexts.add(new Context());

        while (k < maximalContextSize) {
            ArrayList<Context> kPlusOneAssignmentContexts = new ArrayList<>();
            for (Context c : kAssignmentContexts) {
                // for each Context with k domain assignments, construct a Context
                // with k+1 domain assignments by considering adding a single value
                // from the candidateAssignments such that the k-assignment Context
                // hasn't yet fixed a domain in the candidateAssignment's attribute
                for (int a = 1; a < tupleCollection.attributeCount(); a++) {
                    if (!c.isAttributeFixed(tupleCollection.attributeForIndex(a))) {
                        for (ValueDomain d : candidateAssignments.get(a)) {
                            Context newContext = new Context(c);
                            newContext.addDomainAssignment(d);
                            if (!useless(newContext, tupleCollection)) {
                                kPlusOneAssignmentContexts.add(newContext);
                            }
                        }
                    }
                }
            }
            result.addAll(kPlusOneAssignmentContexts);
            kAssignmentContexts = kPlusOneAssignmentContexts;
            k++;
        }

        return result;
    }

    public static void main(String[] args) {
        try {
            TupleCollection tupleCollection = DatabaseUtilities.executeQuery("select model, dollars, pounds, inch_display from macbooks;");
            HybridPlanner planner = new HybridPlanner(2, 2.0, 1);
            VoiceOutputPlan plan = planner.plan(tupleCollection);
            if (plan != null) {
                System.out.println(plan.toSpeechText(false));
            } else {
                System.out.println("Plan was null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
