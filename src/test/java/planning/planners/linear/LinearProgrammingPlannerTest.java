package planning.planners.linear;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;

/**
 * Testing for the LinearProgrammingPlanner
 */
public class LinearProgrammingPlannerTest extends PlannerTestBase {

    public void testLinearPlanNotNull() throws Exception {
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(linearPlanner, tuples, config);
        assertNotNull(result.getPlan());
    }
}
