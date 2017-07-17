package planning.planners;

import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import util.DatabaseUtilities;

/**
 * Testing for the relationship between various planners
 */
public class PlannerComparisonTest extends PlannerTestBase {

    public void testLinearPerformsAsWellAsNaive() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult linearResult = planningManager.buildPlan(linearPlanner, tuples, config);
        PlanningResult naiveResult = planningManager.buildPlan(naivePlanner, tuples, config);

        int linearCost = linearResult.getPlan().toSpeechText(true).length();
        int naiveCost = naiveResult.getPlan().toSpeechText(true).length();

        assertTrue(linearCost <= naiveCost);
    }

    // TODO: compare more plans

}
