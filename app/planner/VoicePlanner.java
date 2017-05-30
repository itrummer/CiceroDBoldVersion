package planner;

import planner.elements.TupleCollection;
import planner.naive.NaiveVoicePlanner;
import sql.Query;
import util.DatabaseUtilities;

import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * Abstract representation of VoicePlanners
 */
public abstract class VoicePlanner {
    public static int DEFAULT_TIMEOUT_MILLIS = 30000;

    /**
     * Visits a TupleCollection to construct a VoiceOutputPlan that represents the contents of rowCollection
     * @param tupleCollection
     * @return The VoiceOutputPlan for the TupleCollection
     */
    public PlanningResult plan(TupleCollection tupleCollection) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AlgorithmCaller algorithmCaller = new AlgorithmCaller(tupleCollection);
        Future<VoiceOutputPlan> future = executor.submit(algorithmCaller);

        boolean timeout;
        long executionTime;
        VoiceOutputPlan plan;

        try {
            long startTime = System.currentTimeMillis();
            plan = future.get(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            long endTime = System.currentTimeMillis();
            if (plan == null) {
                throw new TimeoutException();
            }
            executor.shutdown();
            executionTime = endTime - startTime;
            timeout = false;
        } catch (TimeoutException | InterruptedException | ExecutionException e) {
            plan = new NaiveVoicePlanner().executeAlgorithm(tupleCollection);
            executionTime = DEFAULT_TIMEOUT_MILLIS;
            timeout = true;
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }

        return new PlanningResult(plan)
                .withPlanner(this)
                .withTimeout(timeout)
                .forTuples(tupleCollection)
                .withExecutionTime(executionTime);
    }

    /**
     * Executes the planning algorithm of this VoicePlanner. Allows specification of how many times the
     * algorithm should be executed. Uses the default timeout.
     * @param query
     */
    public PlanningResult plan(Query query) {
        TupleCollection tupleCollection = null;
        try {
            tupleCollection = query.getTupleCollection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return plan(tupleCollection).fromQuery(query);
    }

    protected abstract VoiceOutputPlan executeAlgorithm(TupleCollection tupleCollection);

    public abstract ToleranceConfig getConfig();

    public abstract void setConfig(ToleranceConfig config);

    public abstract String getPlannerName();

    public class AlgorithmCaller implements Callable<VoiceOutputPlan> {
        final TupleCollection tupleCollection;

        public AlgorithmCaller(TupleCollection tupleCollection) {
            this.tupleCollection = tupleCollection;
        }

        @Override
        public VoiceOutputPlan call() throws Exception {
            return executeAlgorithm(tupleCollection);
        }
    }

}
