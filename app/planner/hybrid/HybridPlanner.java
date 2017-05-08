package planner.hybrid;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;
import planner.VoiceOutputPlan;
import planner.VoicePlanner;
import planner.elements.*;
import util.DatabaseUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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

            // TODO: CPLEX variables

            IloIntVar[][] w = new IloIntVar[1+contextCandidates.size()][];
            for (int c = 0; c < w.length; c++) {
                w[c] = cplex.intVarArray(tupleCollection.tupleCount(), 0, 1);
            }

            // constraint: each tuple mapped to one context
            for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                IloLinearIntExpr sumForT = cplex.linearIntExpr();
                for (int c = 0; c < w.length; c++) {
                    sumForT.addTerm(1, w[c][t]);
                }
                cplex.addEq(sumForT, 1);
            }

            IloIntVar[] g = cplex.intVarArray(1 + contextCandidates.size(), 0, 1);
            for (int c = 0; c < w.length; c++) {
                // a context is used only if at least one tuple is output in it
                cplex.addGe(g[c], cplex.sum(w[c]));
            }

            IloLinearIntExpr costExpr = cplex.linearIntExpr();
            for (int c = 0; c < g.length-1; c++) {
                Context context = contextCandidates.get(c);
                costExpr.addTerm(context.toSpeechText(true).length(), g[c]);

                for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                    costExpr.addTerm(tupleCollection.getTuples().get(t).toSpeechText(context, true).length(), w[c][t]);
                }
            }

            // costs for empty context
            for (int t = 0; t < tupleCollection.tupleCount(); t++) {
                costExpr.addTerm(tupleCollection.getTuples().get(t).toSpeechText(true).length(), w[w.length-1][t]);
            }

            cplex.addMinimize(costExpr);

            cplex.solve();

            // parse CPLEX output

            HashMap<Integer, ArrayList<Tuple>> tupleBins = new HashMap<>();

            // create bins for tuples
            for (int c = 0; c < g.length; c++) {
                if (cplex.getValue(g[g.length-1]) > 0.5) {
                    tupleBins.put(c, new ArrayList<>());
                }
            }

            for (int c = 0; c < w.length; c++) {
                for (int t = 0; t < w[c].length; t++) {
                    if (cplex.getValue(w[c][t]) > 0.5) {
                        tupleBins.get(c).add(tupleCollection.getTuples().get(t));
                    }
                }
            }

            ArrayList<Scope> scopes = new ArrayList<>();
            if (tupleBins.containsKey(contextCandidates.size())) {
                // add empty scope if tuples were mapped to it
                scopes.add(new Scope(tupleBins.get(contextCandidates.size())));
            }
            Scope emptyScope;
            for (int c = 0; c < contextCandidates.size(); c++) {
                if (tupleBins.containsKey(c)) {
                    scopes.add(new Scope(contextCandidates.get(c), tupleBins.get(c)));
                }
            }

            plan = new VoiceOutputPlan(scopes);

        } catch (IloException e) {
            e.printStackTrace();
        }

        return plan;
    }

    public boolean useless(Context c, TupleCollection tupleCollection) {
        int totalSavings = 0;
        for (Tuple t : tupleCollection.getTuples()) {
            if (c.matches(t)) {
                int tWithContext = t.toSpeechText(c, true).length();
                int tWithoutContext = t.toSpeechText(true).length();
                totalSavings += tWithoutContext - tWithContext;
            }
        }

        return c.toSpeechText(true).length() >= totalSavings;
    }

    public ArrayList<Context> generateContextCandidates(TupleCollection tupleCollection) {
        HashMap<Integer, HashSet<ValueDomain>> candidateAssignments = tupleCollection.candidateAssignments(maximalCategoricalDomainSize, maximalNumericalDomainWidth);

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
            TupleCollection tupleCollection = DatabaseUtilities.executeQuery("select model, inch_display, hours_battery_life from macbooks;");
            HybridPlanner planner = new HybridPlanner(3, 1.25, 2);
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
