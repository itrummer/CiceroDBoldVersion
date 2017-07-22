package planning.planners.greedy;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;

/**
 * Testing for the GreedyPlanner
 */
public class GreedyPlannerTest extends PlannerTestBase {

    public void testGreedyPlanNotNull() throws Exception {
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(greedyPlanner, tuples, config);
        assertNotNull(result.getPlan());
    }
}
