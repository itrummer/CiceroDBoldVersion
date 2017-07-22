package planning.planners.greedy.fantom;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;

/**
 * Testing for the FantomGreedyPlanner
 */
public class FantomGreedyPlannerTest extends PlannerTestBase {

    public void testNaiveVoicePlannerOutputNotNull() throws Exception {
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0, 0.1);
        PlanningResult result = planningManager.buildPlan(fantomGreedyPlanner, tuples, config);
        assertNotNull(result.getPlan());
    }

    // TODO: test with different epsilon values

}
