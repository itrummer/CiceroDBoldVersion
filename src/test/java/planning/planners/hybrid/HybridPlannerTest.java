package planning.planners.hybrid;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.PlannerTestBase;
import planning.planners.TestTupleCollections;

/**
 * Testing for the HybridPlanner
 */
public class HybridPlannerTest extends PlannerTestBase {

    public void testHybridTop10PlanNotNull() throws Exception {
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(hybridPlannerTop10, tuples, config);
        assertNotNull(result.getPlan());
    }

    public void testHybridTupleCoveringPlanNotNull() throws Exception {
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10", "Restaurants");
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(hybridPlannerTupleCovering, tuples, config);
        assertNotNull(result.getPlan());
    }

    public void testHybridTupleCoveringTupleCollection1() throws Exception {
        TupleCollection tuples = TestTupleCollections.testCollection1();
        Config config = createConfig(2, 2, 2.0);
        PlanningResult result = planningManager.buildPlan(hybridPlannerTupleCovering, tuples, config);
        System.out.println(result.getPlan().toSpeechText(true));
    }
}
