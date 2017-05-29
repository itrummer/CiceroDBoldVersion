package planner;

import junit.framework.TestCase;
import planner.elements.TupleCollection;
import planner.greedy.FantomGreedyPlanner;
import planner.greedy.GreedyPlanner;
import planner.hybrid.*;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import sql.Query;
import util.CSVBuilder;
import util.DatabaseUtilities;
import util.Utilities;
import voice.WatsonVoiceGenerator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit testing for a VoicePlanner
 */
public class VoicePlannerTest extends TestCase {
    public enum TestCase {
        QUERY_1("model, dollars, pounds, inch_display", "macbooks"),
        QUERY_2("restaurant, price, user_rating as \"user rating\", cuisine", "restaurants"),
        QUERY_3("restaurant, price, cuisine", "restaurants"),
        QUERY_4("model, gigabytes_of_memory, gigabytes_of_storage, dollars", "macbooks"),
        QUERY_5("restaurant, user_rating, area, category", "yelp", 40),
        QUERY_6("restaurant, user_rating, price, reviews, area, category", "yelp"),
        QUERY_7("team, wins, touchdowns, conference, total_points_against", "football", 20),
        QUERY_8("model, operating_system, gigabytes_of_storage, gigabytes_of_ram", "phones", 20),
        QUERY_9("model, core_processors, operating_system, grams, gigabytes_of_storage, gigabytes_of_ram", "phones", 20);

        private String attributeList;
        private String relation;
        private String condition;
        private Integer limit;

        TestCase(String attributeList, String relation, String condition, Integer limit) {
            this.attributeList = attributeList;
            this.relation = relation;
            this.condition = condition;
            this.limit = limit;
        }

        TestCase(String attributeList, String relation) {
            this(attributeList, relation, null, null);
        }

        TestCase(String attributeList, String relation, int limit) {
            this(attributeList, relation, null, limit);
        }

        public String getQuery() {
            return "SELECT " + attributeList + " FROM " + relation + (condition != null ? " WHERE " + condition : "") + (limit != null ? " LIMIT " + limit : "");
        }

        public String getRelation() {
            return relation;
        }

        public TupleCollection getTupleCollection() throws SQLException {
            return DatabaseUtilities.executeQuery(getQuery());
        }

        public void setLimit(int limit) {
            this.limit = limit;
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
        PlanningResult result;
        for (TestCase testCase : TestCase.values()) {
            TupleCollection tupleCollection = testCase.getTupleCollection();
            result = planner.plan(tupleCollection, 20);
            System.out.println("naive: " + new NaiveVoicePlanner().plan(tupleCollection).getPlan().toSpeechText(true));
            System.out.println("greedy: " + result.getPlan().toSpeechText(true));
            printResult(planner.getPlannerName(), testCase, result);
        }
    }

    public void testGreedyAllTestCases100Times() throws Exception {
        VoicePlanner planner = new GreedyPlanner(2, 4.0, 1);
        for (TestCase testCase : TestCase.values()) {
            PlanningResult result = planner.plan(testCase.getTupleCollection(), 1);
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

        for (TestCase testCase : new TestCase[] {TestCase.QUERY_1, TestCase.QUERY_2}) {
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

    /**
     * Executes each test case for each planner-config combination
     */
    public String executeTests(TestCase[] testCases, ToleranceConfig[] configs, ContextPruner[] pruners) throws Exception {
        CSVBuilder csvBuilder = new CSVBuilder();
        WatsonVoiceGenerator voiceGenerator = new WatsonVoiceGenerator();
        NaiveVoicePlanner naiveVoicePlanner = new NaiveVoicePlanner();
        LinearProgrammingPlanner linearProgrammingPlanner = new LinearProgrammingPlanner();
        HybridPlanner hybridPlanner = new HybridPlanner();
        GreedyPlanner greedyPlanner = new GreedyPlanner();

        for (TestCase testCase : testCases) {
            TupleCollection tupleCollection = testCase.getTupleCollection();
            String basePath = "/Users/mabryan/temp/" + testCase.name() + "_";

            PlanningResult naiveResult = naiveVoicePlanner.plan(tupleCollection);
            csvBuilder.addTestResult(naiveVoicePlanner, naiveResult, naiveResult, tupleCollection, testCase.name());
            voiceGenerator.generateAndWriteToFile(naiveResult.getPlan().toSpeechText(false),
                    basePath + naiveVoicePlanner.getPlannerName() + ".wav");
            Utilities.writeStringToFile(basePath + naiveVoicePlanner.getPlannerName()+ ".txt", naiveResult.getPlan().toSpeechText(true));

            for (int c = 0; c < configs.length; c++) {
                ToleranceConfig config = configs[c];

                linearProgrammingPlanner.setConfig(config);
                PlanningResult linearResult = linearProgrammingPlanner.plan(tupleCollection);
                int linearCost = linearResult.getPlan().speechCost();
                csvBuilder.addTestResult(linearProgrammingPlanner, linearResult, naiveResult, tupleCollection, testCase.name());
                voiceGenerator.generateAndWriteToFile(linearResult.getPlan().toSpeechText(false),
                        basePath + linearProgrammingPlanner.getPlannerName() + "-config" + c + ".wav");
                Utilities.writeStringToFile(basePath + linearProgrammingPlanner.getPlannerName() + "-config" + c + ".txt", config.toString() + "\n" + linearResult.getPlan().toSpeechText(true));

                hybridPlanner.setConfig(config);
                for (int p = 0; p < pruners.length; p++) {
                    hybridPlanner.setContextPruner(pruners[p]);
                    PlanningResult hybridResult = hybridPlanner.plan(tupleCollection);
//                    assertTrue(testCase.name() + config, hybridResult.getPlan().speechCost() >= linearCost);
                    csvBuilder.addTestResult(hybridPlanner, hybridResult, naiveResult, tupleCollection, testCase.name());
                    voiceGenerator.generateAndWriteToFile(hybridResult.getPlan().toSpeechText(false),
                            basePath + hybridPlanner.getPlannerName() + "-config" + c + ".wav");
                    Utilities.writeStringToFile(basePath + hybridPlanner.getPlannerName() + "-config" + c + ".txt", config.toString() + "\n" + hybridResult.getPlan().toSpeechText(true));
                }

                greedyPlanner.setConfig(config);
                PlanningResult greedyResult = greedyPlanner.plan(tupleCollection);
//                assertTrue(testCase.name() + config.toString(), greedyResult.getPlan().speechCost() >= linearCost);
                csvBuilder.addTestResult(greedyPlanner, greedyResult, naiveResult, tupleCollection, testCase.name());
                voiceGenerator.generateAndWriteToFile(greedyResult.getPlan().toSpeechText(false),
                        basePath + greedyPlanner.getPlannerName() + "-config" + c + ".wav");
                Utilities.writeStringToFile(basePath + greedyPlanner.getPlannerName() + "-config" + c + ".txt", config.toString() + "\n" + greedyResult.getPlan().toSpeechText(true));
            }
        }
        return csvBuilder.getCSVString();
    }

    /**
     * Executes each test case for each planner-config combination
     */
    public String executeTestsWithLimits(TestCase[] testCases, ToleranceConfig[] configs, ContextPruner[] pruners) throws Exception {
        CSVBuilder csvBuilder = new CSVBuilder();
        WatsonVoiceGenerator voiceGenerator = new WatsonVoiceGenerator();
        NaiveVoicePlanner naiveVoicePlanner = new NaiveVoicePlanner();
        LinearProgrammingPlanner linearProgrammingPlanner = new LinearProgrammingPlanner();
        HybridPlanner hybridPlanner = new HybridPlanner();
        FantomGreedyPlanner greedyPlanner = new FantomGreedyPlanner();

        for (TestCase testCase : testCases) {
            TupleCollection tupleCollection = testCase.getTupleCollection();
            String basePath = "/Users/mabryan/temp/" + testCase.name() + "_";

            PlanningResult naiveResult = naiveVoicePlanner.plan(tupleCollection);
            csvBuilder.addTestResult(naiveVoicePlanner, naiveResult, naiveResult, tupleCollection, testCase.name());
            voiceGenerator.generateAndWriteToFile(naiveResult.getPlan().toSpeechText(false),
                    basePath + naiveVoicePlanner.getPlannerName() + ".wav");
            Utilities.writeStringToFile(basePath + naiveVoicePlanner.getPlannerName()+ ".txt", naiveResult.getPlan().toSpeechText(true));

            for (int c = 0; c < configs.length; c++) {
                ToleranceConfig config = configs[c];

                linearProgrammingPlanner.setConfig(config);
                PlanningResult linearResult = linearProgrammingPlanner.plan(tupleCollection);
                int linearCost = linearResult.getPlan().speechCost();
                csvBuilder.addTestResult(linearProgrammingPlanner, linearResult, naiveResult, tupleCollection, testCase.name());
                voiceGenerator.generateAndWriteToFile(linearResult.getPlan().toSpeechText(false),
                        basePath + linearProgrammingPlanner.getPlannerName() + "-config" + c + ".wav");
                Utilities.writeStringToFile(basePath + linearProgrammingPlanner.getPlannerName() + "-config" + c + ".txt", config.toString() + "\n" + linearResult.getPlan().toSpeechText(true));

                hybridPlanner.setConfig(config);
                for (int p = 0; p < pruners.length; p++) {
                    hybridPlanner.setContextPruner(pruners[p]);
                    PlanningResult hybridResult = hybridPlanner.plan(tupleCollection);
//                    assertTrue(testCase.name() + config, hybridResult.getPlan().speechCost() >= linearCost);
                    csvBuilder.addTestResult(hybridPlanner, hybridResult, naiveResult, tupleCollection, testCase.name());
                    voiceGenerator.generateAndWriteToFile(hybridResult.getPlan().toSpeechText(false),
                            basePath + hybridPlanner.getPlannerName() + "-config" + c + ".wav");
                    Utilities.writeStringToFile(basePath + hybridPlanner.getPlannerName() + "-config" + c + ".txt", config.toString() + "\n" + hybridResult.getPlan().toSpeechText(true));
                }

                greedyPlanner.setConfig(config);
                PlanningResult greedyResult = greedyPlanner.plan(tupleCollection);
//                assertTrue(testCase.name() + config.toString(), greedyResult.getPlan().speechCost() >= linearCost);
                csvBuilder.addTestResult(greedyPlanner, greedyResult, naiveResult, tupleCollection, testCase.name());
                voiceGenerator.generateAndWriteToFile(greedyResult.getPlan().toSpeechText(false),
                        basePath + greedyPlanner.getPlannerName() + "-config" + c + ".wav");
                Utilities.writeStringToFile(basePath + greedyPlanner.getPlannerName() + "-config" + c + ".txt", config.toString() + "\n" + greedyResult.getPlan().toSpeechText(true));
            }
        }
        return csvBuilder.getCSVString();
    }

    public void testPlannerGroup1() throws Exception {
        TestCase[] testCases = new TestCase[] {
                TestCase.QUERY_1,
                TestCase.QUERY_2,
                TestCase.QUERY_3,
                TestCase.QUERY_4,
                TestCase.QUERY_5,
        };

        ToleranceConfig[] configs = new ToleranceConfig[] {
                new ToleranceConfig(2, 1.0, 2),
                new ToleranceConfig(2, 2.0, 1),
                new ToleranceConfig(2, 2.0, 2),
                new ToleranceConfig(2, 3.0, 1),
        };

        ContextPruner[] pruners = new ContextPruner[] {
                new TupleCoveringPruner(15),
        };


        String csvResult = executeTests(testCases, configs, pruners);
        Utilities.writeStringToFile("/Users/mabryan/temp/output.csv", csvResult);
    }

    public void testData() throws Exception {
        Query[] queries = new Query[] {
                new Query(new String[] { "model", "dollars" }, "macbooks"),
                new Query(new String[] { "model", "dollars", "gigabytes_of_memory as \"gigabytes of memory\"" }, "macbooks"),
                new Query(new String[] { "restaurant", "user_rating as \"user rating\"" }, "restaurants"),
                new Query(new String[] { "restaurant", "user_rating as \"user rating\"", "cuisine" }, "restaurants"),
                new Query(new String[] { "team", "wins" }, "football"),
                new Query(new String[] { "team", "wins", "conference" }, "football"),
                new Query(new String[] { "model", "operating_system as \"operating system\"" }, "phones"),
                new Query(new String[] { "model", "operating_system as \"operating system\"", "gigabytes_of_storage as \"gigabytes of storage\"" }, "phones"),
        };

        int[] limits = new int[] { 2, 4, 6, 8, 10 };

        int[] mSValues = new int[] { 1, 2 };
        int[] mCValues = new int[] { 1, 2 };
        double[] mWValues = new double[] { 1.0, 1.5, 2.0, 3.0 };
        List<ToleranceConfig> configs = new ArrayList<>();
        for (int mS = 0; mS < mSValues.length; mS++) {
            for (int mC = 0; mC < mCValues.length; mC++) {
                for (int mW = 0; mW < mWValues.length; mW++) {
                    configs.add(new ToleranceConfig(mSValues[mS], mWValues[mW], mCValues[mC]));
                }
            }
        }

        NaiveVoicePlanner naive = new NaiveVoicePlanner();
        HybridPlanner hybridPlanner = new HybridPlanner();
        hybridPlanner.setContextPruner(new TupleCoveringPruner(20));
        LinearProgrammingPlanner linearPlanner = new LinearProgrammingPlanner();

        List<PlanningResult> testResults = new ArrayList<>();

        for (Query query  : queries) {
            for (int limit : limits) {
                query.setLimit(limit);
                testResults.add(naive.plan(query));

                for (ToleranceConfig config : configs) {
                    hybridPlanner.setConfig(config);
                    testResults.add(hybridPlanner.plan(query));

                    linearPlanner.setConfig(config);
                    testResults.add(linearPlanner.plan(query));
                }
            }
        }

        WatsonVoiceGenerator voiceGenerator = new WatsonVoiceGenerator();
        String csvHeader = PlanningResult.getCSVHeader();
        StringBuilder csvLines = new StringBuilder("");
        for (PlanningResult result : testResults) {
            String fileNameBase = result.getFileNameBase();
            String text = "LONGFORM:\n" + result.getPlan().toSpeechText(true) + "\n\nSHORTFORM:\n" + result.getPlan().toSpeechText(false);
            Utilities.writeStringToFile("/Users/mabryan/temp/" + fileNameBase + ".txt", text);
            voiceGenerator.generateAndWriteToFile(result.getPlan().toSpeechText(false), "/Users/mabryan/temp/" + fileNameBase + ".wav");
            csvLines.append(result.getCSVLine() + "\n");
        }

        Utilities.writeStringToFile("/Users/mabryan/temp/results.csv", csvHeader + "\n" + csvLines.toString());
    }



}