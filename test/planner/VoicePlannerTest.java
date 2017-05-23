package planner;

import junit.framework.TestCase;
import planner.elements.TupleCollection;
import planner.greedy.GreedyPlanner;
import planner.hybrid.HybridPlanner;
import planner.hybrid.TupleCoveringPruner;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import util.DatabaseUtilities;

import java.sql.SQLException;

import static org.junit.Assert.*;

/**
 * Unit testing for a VoicePlanner
 */
public class VoicePlannerTest extends TestCase {
    public enum TestCase {
        QUERY_1("model, dollars, pounds, inch_display", "macbooks"),
        QUERY_2("restaurant, price, rating, cuisine", "restaurants"),
        QUERY_3("restaurant, price, cuisine", "restaurants"),
        QUERY_4("model, gigabytes_of_memory, gigabytes_of_storage, dollars", "macbooks"),
        QUERY_5("restaurant, rating, price, reviews, location, cuisine", "yelp"),
        QUERY_6("restaurant, rating, location, cuisine", "yelp");

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

        public TupleCollection getTupleCollection() throws SQLException {
            return DatabaseUtilities.executeQuery(getQuery());
        }
    }

    public void printResult(String plannerName, TestCase testCase, PlanningResult result) {
        System.out.println(plannerName + " planner executed " + testCase.name() +
                " on " + testCase.getRelation() + " " + result.getExecutionCount() +
                " times and took an average "  + result.getAverageExecutionTimeSeconds() +
                " seconds per execution");
    }

    public void testNaivePlanner() throws Exception {
        VoicePlanner planner = new NaiveVoicePlanner();
        for (TestCase testCase : TestCase.values()) {
            PlanningResult result = planner.plan(testCase.getTupleCollection(), 100);
            printResult(planner.getPlannerName(), testCase, result);
        }
    }

    public void testGreedyAllTestCases100Times() throws Exception {
        VoicePlanner planner = new GreedyPlanner(2, 1.5, 1);
        for (TestCase testCase : TestCase.values()) {
            PlanningResult result = planner.plan(testCase.getTupleCollection(), 100);
            printResult(planner.getPlannerName(), testCase, result);
        }
    }

    public void testGreedyPlannerQuery1() throws Exception {
        GreedyPlanner planner = new GreedyPlanner(2, 1.5, 1);
        PlanningResult result;
        result = planner.plan(TestCase.QUERY_1.getTupleCollection(), 10);
        printResult(planner.getPlannerName(), TestCase.QUERY_2, result);
        result = planner.plan(TestCase.QUERY_1.getTupleCollection(), 100);
        printResult(planner.getPlannerName(), TestCase.QUERY_2, result);
        result = planner.plan(TestCase.QUERY_1.getTupleCollection(), 1000);
        printResult(planner.getPlannerName(), TestCase.QUERY_2, result);
    }

    public void testGreedyPlannerOver10ExecutionsQuery2() throws Exception {
        GreedyPlanner planner = new GreedyPlanner(2, 1.5, 1);
        PlanningResult result;
        result = planner.plan(TestCase.QUERY_2.getTupleCollection(), 10);
        printResult(planner.getPlannerName(), TestCase.QUERY_2, result);
        result = planner.plan(TestCase.QUERY_2.getTupleCollection(), 100);
        printResult(planner.getPlannerName(), TestCase.QUERY_2, result);
        result = planner.plan(TestCase.QUERY_2.getTupleCollection(), 1000);
        printResult(planner.getPlannerName(), TestCase.QUERY_2, result);
    }

    public void testHybridAllTestCases100Times() throws Exception {
        HybridPlanner planner = new HybridPlanner(new TupleCoveringPruner(30), 3, 2.0, 1);
        for (TestCase testCase : TestCase.values()) {
            PlanningResult result = planner.plan(testCase.getTupleCollection(), 100);
            printResult(planner.getPlannerName(), testCase, result);
        }
    }

    public void testGreedyPlannerQuery6() throws Exception {
        GreedyPlanner planner = new GreedyPlanner(2, 2.0, 1);
        PlanningResult result = planner.plan(DatabaseUtilities.executeQuery(TestCase.QUERY_6.getQuery()));
        printResult(planner.getPlannerName(), TestCase.QUERY_6, result);
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
            PlanningResult result;
            TupleCollection tupleCollection = testCase.getTupleCollection();

            result = linear.plan(tupleCollection);
            System.out.println("Linear\n" + result.getPlan().toSpeechText(false));
            int linearCost = result.getPlan().toSpeechText(true).length();

            result = greedy.plan(tupleCollection);
            int greedyCost = result.getPlan().toSpeechText(true).length();
            System.out.println("Greedy\n" + result.getPlan().toSpeechText(false));
            assertTrue(testCase.name(),linearCost <= greedyCost);

            result = hybrid.plan(tupleCollection);
            int hybridCost = result.getPlan().toSpeechText(true).length();
            System.out.println("Hybrid\n" + result.getPlan().toSpeechText(false));
            assertTrue(testCase.name(), linearCost <= hybridCost);
        }
    }

    public void testLinearHasSmallestCostManyConfigs() throws Exception {
        testLinearHasSmallestCost(2, 2.0, 2, 10);
        testLinearHasSmallestCost(2, 2.0, 2, 30);
        testLinearHasSmallestCost(2, 3.0, 2, 30);
        testLinearHasSmallestCost(3, 2.0, 2, 10);
        testLinearHasSmallestCost(2, 1.0, 2, 10);
        testLinearHasSmallestCost(3, 1.5, 2, 10);
    }

}