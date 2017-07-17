package planning.planners.greedy.fantom;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;
import util.DatabaseUtilities;

/**
 * Testing for the FantomGreedyPlanner
 */
public class FantomGreedyPlannerTest extends PlannerTestBase {

    public void testNaiveVoicePlannerOutputNotNull() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0, 0.1);
        PlanningResult result = planningManager.buildPlan(fantomGreedyPlanner, tuples, config);
        assertNotNull(result.getPlan());
    }
}
