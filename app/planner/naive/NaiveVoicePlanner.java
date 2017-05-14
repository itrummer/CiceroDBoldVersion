package planner.naive;

import planner.VoiceOutputPlan;
import planner.VoicePlanner;
import planner.elements.TupleCollection;
import planner.elements.Scope;
import util.DatabaseUtilities;

/**
 * A naive implementation of a voice plan. Lists all results in a query as individual tuples.
 */
public class NaiveVoicePlanner extends VoicePlanner {

    @Override
    public VoiceOutputPlan plan(TupleCollection tupleCollection) {
        VoiceOutputPlan outputPlan = new VoiceOutputPlan();
        outputPlan.addScope(new Scope(tupleCollection.getTuples()));
        return outputPlan;
    }

    public static void main(String[] args) {
        try {
            TupleCollection tupleCollection = DatabaseUtilities.executeQuery("select * from macbooks;");
            NaiveVoicePlanner planner = new NaiveVoicePlanner();
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
