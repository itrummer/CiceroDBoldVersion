package planner;

import junit.framework.TestCase;
import planner.elements.TupleCollection;
import planner.greedy.FantomGreedyPlanner;
import planner.greedy.GreedyPlanner;
import planner.hybrid.*;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import sql.Query;
import sql.Relation;
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

//    /**
//     * Executes each planner with the same configuration and asserts that the linear algorithm's
//     * output plan has the smallest speech cost
//     * @param k The k value to use in configuring the HybridPlanner
//     */
//    public void testLinearHasSmallestCost(int mS, double mW, int mC, int k) throws Exception {
//        VoicePlanner linear = new LinearProgrammingPlanner(mS, mW, mC);
//        VoicePlanner greedy = new GreedyPlanner(mS, mW, mC);
//        VoicePlanner hybrid = new HybridPlanner(new TupleCoveringPruner(k), mS, mW, mC);
//
//        for (TestCase testCase : new TestCase[] {TestCase.QUERY_1, TestCase.QUERY_2}) {
//            PlanningResult result;
//            TupleCollection tupleCollection = testCase.getTupleCollection();
//
//            result = linear.plan(tupleCollection);
//            System.out.println("Linear\n" + result.getPlan().toSpeechText(false));
//            int linearCost = result.getPlan().toSpeechText(true).length();
//
//            result = greedy.plan(tupleCollection);
//            int greedyCost = result.getPlan().toSpeechText(true).length();
//            System.out.println("Greedy\n" + result.getPlan().toSpeechText(false));
//            assertTrue(testCase.name(),linearCost <= greedyCost);
//
//            result = hybrid.plan(tupleCollection);
//            int hybridCost = result.getPlan().toSpeechText(true).length();
//            System.out.println("Hybrid\n" + result.getPlan().toSpeechText(false));
//            assertTrue(testCase.name(), linearCost <= hybridCost);
//        }
//    }
//
//    public void testLinearHasSmallestCostManyConfigs() throws Exception {
//        testLinearHasSmallestCost(2, 2.0, 2, 10);
//        testLinearHasSmallestCost(2, 2.0, 2, 30);
//        testLinearHasSmallestCost(2, 3.0, 2, 30);
//        testLinearHasSmallestCost(3, 2.0, 2, 10);
//        testLinearHasSmallestCost(2, 1.0, 2, 10);
//        testLinearHasSmallestCost(3, 1.5, 2, 10);
//    }

    public void testBriefPlannerGroup() throws Exception {
        Query[] queries = new Query[] {
                new Query(new String[] { "model", "dollars", "pounds", "inch_display as \"inch display\"" }, Relation.MACBOOKS),
                new Query(new String[] { "model", "gigabytes_of_storage", "dollars" }, Relation.MACBOOKS),
                new Query(new String[] { "restaurant", "price", "user_rating as \"user rating\"", "cuisine" }, Relation.RESTAURANTS),
                new Query(new String[] { "restaurant", "price", "cuisine" }, Relation.RESTAURANTS),
                new Query(new String[] { "restaurant", "user_rating", "area", "category" }, Relation.YELP),
        };

        ToleranceConfig[] configs = new ToleranceConfig[] {
                new ToleranceConfig(2, 1.0, 1),
                new ToleranceConfig(2, 2.0, 1),
                new ToleranceConfig(2, 3.0, 2),
        };

        NaiveVoicePlanner naive = new NaiveVoicePlanner();
        HybridPlanner hybridPlanner = new HybridPlanner();
        hybridPlanner.setContextPruner(new TupleCoveringPruner(20));
        LinearProgrammingPlanner linearPlanner = new LinearProgrammingPlanner();
        FantomGreedyPlanner greedyPlanner = new FantomGreedyPlanner();

        List<PlanningResult> testResults = new ArrayList<>();

        for (Query query : queries) {
            query.setLimit(40);
            testResults.add(naive.plan(query));

            for (ToleranceConfig config : configs) {
                hybridPlanner.setConfig(config);
                testResults.add(hybridPlanner.plan(query));

                linearPlanner.setConfig(config);
                testResults.add(linearPlanner.plan(query));

                greedyPlanner.setConfig(config);
                testResults.add(greedyPlanner.plan(query));
            }
        }

//        WatsonVoiceGenerator voiceGenerator = new WatsonVoiceGenerator();
        String csvHeader = PlanningResult.getCSVHeader();
        StringBuilder csvLines = new StringBuilder("");
        for (PlanningResult result : testResults) {
            String fileNameBase = result.getFileNameBase();
            String text = "LONGFORM:\n" + result.getPlan().toSpeechText(true) + "\n\nSHORTFORM:\n" + result.getPlan().toSpeechText(false);
            Utilities.writeStringToFile("/Users/mabryan/temp/" + fileNameBase + ".txt", text);
//            voiceGenerator.generateAndWriteToFile(result.getPlan().toSpeechText(false), "/Users/mabryan/temp/" + fileNameBase + ".wav");
            csvLines.append(result.getCSVLine() + "\n");
        }

        Utilities.writeStringToFile("/Users/mabryan/temp/results.csv", csvHeader + "\n" + csvLines.toString());
    }

    public void testData() throws Exception {
        int[] mSValues = new int[] { 1, 2 };
        int[] mCValues = new int[] { 1, 2 };
        double[] mWValues = new double[] { 1.0, 2.0, 4.0 };
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
        FantomGreedyPlanner greedyPlanner1 = new FantomGreedyPlanner(1.0);
        FantomGreedyPlanner greedyPlanner2 = new FantomGreedyPlanner(5.0);

        List<PlanningResult> testResults = new ArrayList<>();

        Relation[] testRelations = new Relation[] { Relation.MACBOOKS, Relation.RESTAURANTS, Relation.FOOTBALL, Relation.PHONES };
        int[][] limitsForRelation = new int[][] {
                { 2, 4, 6, 8, 10 },
                { 2, 4, 6, 8, 10 },
                { 2, 4, 6, 8, 10, 20 },
                { 2, 4, 6, 8, 10, 20, 30, 40, 50 },
        };

        int[][] numberColumns = new int[][]{
                { 1, 2 },
                { 1, 2 },
                { 1, 2 },
                { 1, 2 },
        };

        int relationIndex = 0;
        for (Relation relation : testRelations) {
            System.out.println("Testing relation " + relation.getName());
            for (int cols : numberColumns[relationIndex]) {
                Query query = relation.queryWithColumns(cols);
                System.out.println("QUERY: " + query.getQuery());
                for (int limit : limitsForRelation[relationIndex]) {
                    query.setLimit(limit);
                    PlanningResult naiveResult = naive.plan(query);
                    testResults.add(naiveResult);

                    for (ToleranceConfig config : configs) {
                        hybridPlanner.setConfig(config);
                        testResults.add(hybridPlanner.plan(query).withCorrespondingNaive(naiveResult));

                        linearPlanner.setConfig(config);
                        testResults.add(linearPlanner.plan(query).withCorrespondingNaive(naiveResult));

                        greedyPlanner1.setConfig(config);
                        testResults.add(greedyPlanner1.plan(query).withCorrespondingNaive(naiveResult));

                        greedyPlanner2.setConfig(config);
                        testResults.add(greedyPlanner2.plan(query).withCorrespondingNaive(naiveResult));
                    }
                }
            }
            relationIndex++;
        }

        System.out.println("Done generating results. Building analytics CSV...");

        WatsonVoiceGenerator voiceGenerator = new WatsonVoiceGenerator();
        String csvHeader = PlanningResult.getCSVHeader();
        StringBuilder csvLines = new StringBuilder("");
        for (PlanningResult result : testResults) {
            String fileNameBase = result.getFileNameBase();
            System.out.println("Evaluating test case: " + fileNameBase);
            String text = "LONGFORM:\n" + result.getPlan().toSpeechText(true) + "\n\nSHORTFORM:\n" + result.getPlan().toSpeechText(false);
            Utilities.writeStringToFile("/Users/mabryan/temp/" + fileNameBase + ".txt", text);
            String wavFilePath = "/Users/mabryan/temp/" + fileNameBase + ".wav";
            voiceGenerator.generateAndWriteToFile(result.getPlan().toSpeechText(false), wavFilePath);
            result.setAudioSpeechCost(Utilities.audioLengthInSeconds(wavFilePath));
            csvLines.append(result.getCSVLine() + "\n");
        }

        Utilities.writeStringToFile("/Users/mabryan/temp/results.csv", csvHeader + "\n" + csvLines.toString());
    }



}