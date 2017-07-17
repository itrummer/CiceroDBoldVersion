package planning.planners.linear;

import junit.framework.TestCase;
import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;
import util.DatabaseUtilities;

/**
 * Testing for the LinearProgrammingPlanner
 */
public class LinearProgrammingPlannerTest extends PlannerTestBase {

    public void testGreedyPlanNotNull() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(linearPlanner, tuples, config);
        assertNotNull(result.getPlan());
    }
}