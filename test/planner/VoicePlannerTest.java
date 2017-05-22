package planner;

import junit.framework.TestCase;
import planner.elements.TupleCollection;
import planner.greedy.GreedyPlanner;
import planner.hybrid.HybridPlanner;
import planner.hybrid.TupleCoveringPruner;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import util.DatabaseUtilities;

import static org.junit.Assert.*;

/**
 * Unit testing for a VoicePlanner
 */
public class VoicePlannerTest extends TestCase {
    public enum TestCase {
        QUERY_1("model, dollars, pounds, inch_display", "macbooks"),
        QUERY_2("restaurant, price, rating, cuisine", "restaurants"),
        QUERY_3("restaurant, price, cuisine", "restaurants"),
        QUERY_4("model, gigabytes_of_memory, gigabytes_of_storage, dollars", "macbooks");

        private String attributeList;
        private String relation;

        TestCase(String attributeList, String relation) {
            this.attributeList = attributeList;
            this.relation = relation;
        }

        public String getQuery() {
            return "SELECT " + attributeList + " FROM " + relation;
        }

        public String getRelation() {
            return relation;
        }
    }

    /**
     * Executes the given planner n times on tupleCollection. Calculates the average execution time by
     * dividing total execution time in milliseconds by the number of executions n.
     */
    private double testPlannerAveragePlanningTime(int n, VoicePlanner planner, TestCase testCase) throws Exception{
        TupleCollection tuples = DatabaseUtilities.executeQuery(testCase.getQuery());
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            planner.plan(tuples);
        }
        long endTime = System.currentTimeMillis();
        long elapsedMillis = endTime - startTime;
        double elapsedSeconds = elapsedMillis / 1000.0;
        double averageSeconds = elapsedSeconds / n;
        System.out.println(planner.getPlannerName() + " planner executed " + testCase.name() + " on " +
                testCase.getRelation() + " " + n + " times and took an average " + averageSeconds +
                " seconds per execution");
        return averageSeconds;
    }

    public void testNaivePlanner() throws Exception {
        VoicePlanner planner = new NaiveVoicePlanner();
        for (TestCase testCase : TestCase.values()) {
            testPlannerAveragePlanningTime(100, planner, testCase);
        }
    }

    public void testGreedyAllTestCases100Times() throws Exception {
        VoicePlanner planner = new GreedyPlanner(2, 1.5, 1);
        for (TestCase testCase : TestCase.values()) {
            testPlannerAveragePlanningTime(100, planner, testCase);
        }
    }

    public void testGreedyPlannerQuery1() throws Exception {
        GreedyPlanner planner = new GreedyPlanner(2, 1.5, 1);
        testPlannerAveragePlanningTime(10, planner, TestCase.QUERY_1);
        testPlannerAveragePlanningTime(100, planner, TestCase.QUERY_1);
        testPlannerAveragePlanningTime(1000, planner, TestCase.QUERY_1);
    }

    public void testGreedyPlannerOver10ExecutionsQuery2() throws Exception {
        GreedyPlanner planner = new GreedyPlanner(2, 1.5, 1);
        testPlannerAveragePlanningTime(10, planner, TestCase.QUERY_2);
        testPlannerAveragePlanningTime(100, planner, TestCase.QUERY_2);
    }

    /**
     * Executes each planner with the same configuration and asserts that the linear algorithm's
     * output plan has the smallest speech cost
     * @param k The k value to use in configuring the HybridPlanner
     */
    public void testLinearHasSmallestCost(int mS, double mW, int mC, int k) throws Exception {
        VoicePlanner linear = new LinearProgrammingPlanner(mS, mW, mC);
        VoicePlanner greedy = new GreedyPlanner(mS, mW, mC);
        VoicePlanner hybrid = new HybridPlanner(new TupleCoveringPruner(k), mS, mW, mC);

        for (TestCase testCase : TestCase.values()) {
            TupleCollection tupleCollection = DatabaseUtilities.executeQuery(testCase.getQuery());
            VoiceOutputPlan linearPlan = linear.plan(tupleCollection);

            VoiceOutputPlan greedyPlan = greedy.plan(tupleCollection);
            assertTrue(linearPlan.toSpeechText(true).length() <= greedyPlan.toSpeechText(true).length());

            VoiceOutputPlan hybridPlan = hybrid.plan(tupleCollection);
            assertTrue(linearPlan.toSpeechText(true).length() <= hybridPlan.toSpeechText(true).length());
        }
    }

    public void testLinearHasSmallestCostManyConfigs() throws Exception {
        testLinearHasSmallestCost(2, 2.0, 2, 10);
        testLinearHasSmallestCost(3, 2.0, 2, 10);
        testLinearHasSmallestCost(2, 2.5, 1, 10);
        testLinearHasSmallestCost(2, 1.0, 2, 10);
    }


}