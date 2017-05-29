package planner;

import planner.elements.TupleCollection;
import planner.naive.NaiveVoicePlanner;
import sql.Query;

import java.sql.SQLException;
import java.util.concurrent.*;

/**
 * Abstract representation of VoicePlanners
 */
public abstract class VoicePlanner {
    public static int DEFAULT_TIMEOUT_SECONDS = 400;

    /**
     * Visits a TupleCollection to construct a VoiceOutputPlan that represents the contents of rowCollection
     * @param tupleCollection
     * @return The VoiceOutputPlan for the TupleCollection
     */
    public PlanningResult plan(TupleCollection tupleCollection) {
        return plan(tupleCollection, 1);
    }

    /**
     * Executes the planning algorithm of this VoicePlanner. Allows specification of how many times the
     * algorithm should be executed. Uses the default timeout.
     * @param tupleCollection
     * @param n The number of times to execute the algorithm
     */
    public PlanningResult plan(TupleCollection tupleCollection, int n) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<VoiceOutputPlan> task = new Callable<VoiceOutputPlan>() {
            public VoiceOutputPlan call() {
                return executeAlgorithm(tupleCollection);
            }
        };

        Future<VoiceOutputPlan> future = executor.submit(task);

        try {
            long startTime = System.currentTimeMillis();
            VoiceOutputPlan plan = null;
            for (int i = 0; i < n; i++) {
                plan = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            long endTime = System.currentTimeMillis();
            return new PlanningResult(plan, endTime - startTime, n, false);
        } catch (TimeoutException ex) {
            VoiceOutputPlan plan = new NaiveVoicePlanner().executeAlgorithm(tupleCollection);
            return new PlanningResult(plan, DEFAULT_TIMEOUT_SECONDS * 1000, n, true);
        } catch (InterruptedException e) {
            // handle the interrupts
        } catch (ExecutionException e) {
            // handle other exceptions
        } finally {
            future.cancel(true);
        }

        return null;
    }

    public PlanningResult plan(Query query) {
        TupleCollection tuples;
        try {
            tuples = query.getTupleCollection();
        } catch (SQLException e) {
            return null;
        }

        long startTime = System.currentTimeMillis();
        VoiceOutputPlan plan = executeAlgorithm(tuples);
        long endTime = System.currentTimeMillis();

        return new PlanningResult(plan, endTime - startTime, false, query.getColumns(), tuples.tupleCount(), this, query.getRelation());
    }

    protected abstract VoiceOutputPlan executeAlgorithm(TupleCollection tupleCollection);

    public abstract ToleranceConfig getConfig();

    public abstract void setConfig(ToleranceConfig config);

    public abstract String getPlannerName();

}
