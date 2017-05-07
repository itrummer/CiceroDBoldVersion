package planner.hybrid;

import planner.VoiceOutputPlan;
import planner.VoicePlanner;
import planner.elements.Context;
import planner.elements.Tuple;
import planner.elements.TupleCollection;
import planner.elements.ValueDomain;
import util.DatabaseUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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



        return null;
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
