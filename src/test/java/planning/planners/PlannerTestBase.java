package planning.planners;

import junit.framework.TestCase;
import planning.PlanningManager;
import planning.config.Config;
import planning.planners.greedy.GreedyPlanner;
import planning.planners.greedy.fantom.FantomGreedyPlanner;
import planning.planners.hybrid.HybridPlanner;
import planning.planners.hybrid.TopKPruner;
import planning.planners.hybrid.TupleCoveringPruner;
import planning.planners.linear.LinearProgrammingPlanner;
import planning.planners.naive.NaiveVoicePlanner;

/**
 * Base class for writing VoicePlanningTest. Provides utility methods for test setup
 */
public class PlannerTestBase extends TestCase {
    protected PlanningManager planningManager = new PlanningManager();
    protected NaiveVoicePlanner naivePlanner = new NaiveVoicePlanner();
    protected LinearProgrammingPlanner linearPlanner = new LinearProgrammingPlanner();
    protected HybridPlanner hybridPlannerTop10 = new HybridPlanner(new TopKPruner(10));
    protected HybridPlanner hybridPlannerTupleCovering = new HybridPlanner(new TupleCoveringPruner(10));
    protected FantomGreedyPlanner fantomGreedyPlanner = new FantomGreedyPlanner();
    protected GreedyPlanner greedyPlanner = new GreedyPlanner();

    protected Config createConfig(int mS, int mC, double mW) {
        Config config = new Config();
        config.setMaxAllowableContextSize(mS);
        config.setMaxAllowableCategoricalDomainSize(mC);
        config.setMaxAllowableNumericalDomainWidth(mW);
        return config;
    }

    protected Config createConfig(int mS, int mC, double mW, double epsilon) {
        Config config = createConfig(mS, mC, mW);
        config.setEpsilon(epsilon);
        return config;
    }
}
