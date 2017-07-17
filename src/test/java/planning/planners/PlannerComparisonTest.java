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

    public void testHybridTop10PerformsAsWellAsNaive() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult hybridResult = planningManager.buildPlan(hybridPlannerTop10, tuples, config);
        PlanningResult naiveResult = planningManager.buildPlan(naivePlanner, tuples, config);

        int hybridCost = hybridResult.getPlan().toSpeechText(true).length();
        int naiveCost = naiveResult.getPlan().toSpeechText(true).length();

        assertTrue(hybridCost <= naiveCost);
    }

    public void testHybridTupleCoveringPerformsAsWellAsNaive() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult hybridResult = planningManager.buildPlan(hybridPlannerTupleCovering, tuples, config);
        PlanningResult naiveResult = planningManager.buildPlan(naivePlanner, tuples, config);

        int hybridCost = hybridResult.getPlan().toSpeechText(true).length();
        int naiveCost = naiveResult.getPlan().toSpeechText(true).length();

        assertTrue(hybridCost <= naiveCost);
    }

    public void testFantomGreedyPerformsAsWellAsNaive() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);
        config.setEpsilon(0.1);

        PlanningResult greedyResult = planningManager.buildPlan(fantomGreedyPlanner, tuples, config);
        PlanningResult naiveResult = planningManager.buildPlan(naivePlanner, tuples, config);

        int greedyCost = greedyResult.getPlan().toSpeechText(true).length();
        int naiveCost = naiveResult.getPlan().toSpeechText(true).length();

        assertTrue(greedyCost <= naiveCost);
    }

    public void testGreedyPerformsAsWellAsNaive() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult greedyResult = planningManager.buildPlan(greedyPlanner, tuples, config);
        PlanningResult naiveResult = planningManager.buildPlan(naivePlanner, tuples, config);

        int greedyCost = greedyResult.getPlan().toSpeechText(true).length();
        int naiveCost = naiveResult.getPlan().toSpeechText(true).length();

        assertTrue(greedyCost <= naiveCost);
    }

    public void testLinearPerformsAsWellAsHybridTop10() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult linearResult = planningManager.buildPlan(linearPlanner, tuples, config);
        PlanningResult hybridResult = planningManager.buildPlan(hybridPlannerTop10, tuples, config);

        int linearCost = linearResult.getPlan().toSpeechText(true).length();
        int hybridCost = hybridResult.getPlan().toSpeechText(true).length();

        assertTrue(linearCost <= hybridCost);
    }

    public void testLinearPerformsAsWellAsHybridTupleCovering() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult linearResult = planningManager.buildPlan(linearPlanner, tuples, config);
        PlanningResult hybridResult = planningManager.buildPlan(hybridPlannerTupleCovering, tuples, config);

        int linearCost = linearResult.getPlan().toSpeechText(true).length();
        int hybridCost = hybridResult.getPlan().toSpeechText(true).length();

        assertTrue(linearCost <= hybridCost);
    }

    public void testLinearPerformsAsWellAsFantom() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);
        config.setEpsilon(0.1);

        PlanningResult linearResult = planningManager.buildPlan(linearPlanner, tuples, config);
        PlanningResult fantomResult = planningManager.buildPlan(fantomGreedyPlanner, tuples, config);

        int linearCost = linearResult.getPlan().toSpeechText(true).length();
        int fantomCost = fantomResult.getPlan().toSpeechText(true).length();

        assertTrue(linearCost <= fantomCost);
    }

    public void testLinearPerformsAsWellAsGreedy() throws Exception {
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");

        Config config = new Config();
        config.setTimeout(120);
        config.setMaxAllowableContextSize(2);
        config.setMaxAllowableCategoricalDomainSize(2);
        config.setMaxAllowableNumericalDomainWidth(2.5);

        PlanningResult linearResult = planningManager.buildPlan(linearPlanner, tuples, config);
        PlanningResult greedyResult = planningManager.buildPlan(greedyPlanner, tuples, config);

        int linearCost = linearResult.getPlan().toSpeechText(true).length();
        int greedyCost = greedyResult.getPlan().toSpeechText(true).length();

        assertTrue(linearCost <= greedyCost);
    }
}
