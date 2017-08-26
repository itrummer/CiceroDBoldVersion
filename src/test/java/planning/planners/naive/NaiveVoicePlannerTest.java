package planning.planners.naive;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.elements.TupleCollectionTest;
import planning.planners.PlannerTestBase;
import planning.planners.TestTupleCollections;

/**
 * Testing for the NaiveVoicePlanner
 */
public class NaiveVoicePlannerTest extends PlannerTestBase {

    /**
     * Test that the NaiveVoicePlanner produces a plan
     * @throws Exception
     */
    public void testNaiveVoicePlannerOutputNotNull() throws Exception {
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10");
        PlanningResult result = planningManager.buildPlan(naivePlanner, tuples, new Config());
        assertNotNull(result.getPlan());
    }

    public void testNaiveTupleCollection1() {
        TupleCollection tuples = TestTupleCollections.testCollection1();
        PlanningResult result = planningManager.buildPlan(naivePlanner, tuples, new Config());
        System.out.println(result.getPlan().toSpeechText(true));
    }

}
