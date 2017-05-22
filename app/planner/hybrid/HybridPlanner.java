package planner.hybrid;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import planner.ToleranceConfig;
import planner.VoiceOutputPlan;
import planner.VoicePlanner;
import planner.elements.*;
import planner.greedy.GreedyPlanner;
import util.DatabaseUtilities;

import java.util.*;

/**
 */
public class HybridPlanner extends VoicePlanner {
    private ContextPruner contextPruner;
    private int maximalContextSize;
    private double maximalNumericalDomainWidth;
    private int maximalCategoricalDomainSize;

    public HybridPlanner(ContextPruner contextPruner, int mS, double mW, int mC) {
        this.contextPruner = contextPruner;
        this.maximalContextSize = mS;
        this.maximalNumericalDomainWidth = mW;
        this.maximalCategoricalDomainSize = mC;
    }

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        ArrayList<Context> contextCandidates = generateContextCandidates(tupleCollection);
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
                Tuple tuple = tupleCollection.getTuple(t);
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
                boolean matched = false;
                for (int c = 0; c < contextCount; c++) {
                    if (cplex.getValue(w[c][t]) > 0.5) {
                        tupleBins.get(c).add(tupleCollection.getTuple(t));
                        matched = true;
                    }
                }
                if (!matched) {
                    emptyContextTuples.add(tupleCollection.getTuple(t));
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

            plan = new VoiceOutputPlan(scopes);
        } catch (IloException e) {
            e.printStackTrace();
        }

        return plan;
    }

    public ArrayList<Context> generateContextCandidates(TupleCollection tupleCollection) {
        Map<Integer, Set<ValueDomain>> candidateAssignments = tupleCollection.candidateAssignments(maximalCategoricalDomainSize, maximalNumericalDomainWidth);

        ArrayList<Context> result = new ArrayList<>();

        int k = 0;
        Collection<Context> kAssignmentContexts = new ArrayList<>();
        kAssignmentContexts.add(new Context());

        while (k < maximalContextSize) {
            Collection<Context> kPlusOneAssignmentContexts = new ArrayList<>();
            for (Context c : kAssignmentContexts) {
                ArrayList<Context> unfiltered = new ArrayList<>();
                for (int a = 1; a < tupleCollection.attributeCount(); a++) {
                    if (!c.isAttributeFixed(tupleCollection.attributeForIndex(a))) {
                        for (ValueDomain d : candidateAssignments.get(a)) {
                            Context newContext = new Context(c);
                            newContext.addDomainAssignment(d);
                            unfiltered.add(newContext);
                        }
                    }
                }
                kPlusOneAssignmentContexts = contextPruner.prune(unfiltered, tupleCollection);
            }
            result.addAll(kPlusOneAssignmentContexts);
            kAssignmentContexts = kPlusOneAssignmentContexts;
            k++;
        }

        return result;
    }

    @Override
    public String getPlannerName() {
        return "hybrid" + contextPruner.getName();
    }

    @Override
    public ToleranceConfig getConfig() {
        return new ToleranceConfig(maximalContextSize, maximalNumericalDomainWidth, maximalCategoricalDomainSize);
    }
}
