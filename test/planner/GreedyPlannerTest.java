package planner;

import org.junit.Test;
import planner.elements.TupleCollection;
import util.DatabaseUtilities;

import static org.junit.Assert.*;

/**
 * Created by mabryan on 4/30/17.
 */
public class GreedyPlannerTest {
    @Test
    public void testPlan() throws Exception {
        TupleCollection tupleCollection = DatabaseUtilities.executeQuery("select * from restaurants;");
        GreedyPlanner planner = new GreedyPlanner();
        VoiceOutputPlan plan = planner.plan(tupleCollection);
        System.out.println(plan.toSpeechText(false));
    }

}