package planning.planners.hybrid;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;
import util.DatabaseUtilities;

/**
 * Testing for the HybridPlanner
 */
public class HybridPlannerTest extends PlannerTestBase {

    public void testHybridTop10PlanNotNull() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(hybridPlannerTop10, tuples, config);
        assertNotNull(result.getPlan());
    }

    public void testHybridTupleCoveringPlanNotNull() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(hybridPlannerTupleCovering, tuples, config);
        assertNotNull(result.getPlan());
    }
}
