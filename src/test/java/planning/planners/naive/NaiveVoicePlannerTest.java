package planning.planners.naive;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;
import util.DatabaseUtilities;

/**
 * Testing for the NaiveVoicePlanner
 */
public class NaiveVoicePlannerTest extends PlannerTestBase {

    /**
     * Test that the NaiveVoicePlanner produces a plan
     * @throws Exception
     */
    public void testNaiveVoicePlannerOutputNotNull() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");
        PlanningResult result = planningManager.buildPlan(naivePlanner, tuples, new Config());
        assertNotNull(result.getPlan());
    }
}
