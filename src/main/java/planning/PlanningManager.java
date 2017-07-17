package planning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.naive.NaiveVoicePlanner;

import java.util.concurrent.*;

public class PlanningManager {
    private Logger logger = LoggerFactory.getLogger(PlanningManager.class);
    VoicePlanner defaultPlanner;

    /**
     * Lightweight class to manage exection of arbitrary planning algorithms for
     * arbitrary configurations. Manages the execution process and records any
     * metadata necessary for producing a PlanningResult.
     */
    public PlanningManager() {
        defaultPlanner = new NaiveVoicePlanner();
    }

    /**
     *
     * @param planner
     * @param tuples
     * @param config
     * @return
     */
    public PlanningResult buildPlan(VoicePlanner planner, TupleCollection tuples, Config config) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AlgorithmCaller algorithmCaller = new AlgorithmCaller(tuples, planner, config);
        Future<VoiceOutputPlan> future = executor.submit(algorithmCaller);

        logger.debug("Beginning planning");
        VoiceOutputPlan plan;
        long startTime = System.currentTimeMillis();

        try {
            plan = future.get(config.getTimeout(), TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            logger.debug(String.format("%s timed out, defaulting to %s", planner.getPlannerIdentifier(), defaultPlanner.getPlannerIdentifier()));
            plan = defaultPlanner.plan(tuples, config);
        }

        long endTime = System.currentTimeMillis();
        logger.debug("Finished planning");

        return new PlanningResult(plan, tuples, config, planner.getPlannerIdentifier(), endTime - startTime);
    }

    public class AlgorithmCaller implements Callable<VoiceOutputPlan> {
        final TupleCollection tupleCollection;
        final VoicePlanner planner;
        final Config config;

        public AlgorithmCaller(TupleCollection tupleCollection, VoicePlanner planner, Config config) {
            this.tupleCollection = tupleCollection;
            this.planner = planner;
            this.config = config;
        }

        @Override
        public VoiceOutputPlan call() throws Exception {
            VoiceOutputPlan plan = planner.plan(tupleCollection, config);
            if (plan == null) {
                throw new TimeoutException();
            }
            return plan;
        }
    }

}
