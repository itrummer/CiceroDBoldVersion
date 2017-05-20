package planner;

import planner.elements.TupleCollection;
import planner.greedy.GreedyPlanner;
import planner.hybrid.HybridPlanner;
import planner.hybrid.TupleCoveringPruner;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import util.DatabaseUtilities;
import voice.WatsonVoiceGenerator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class TestRunner {
    static final String CSV_COLUMN_QUERY_ID = "query_id";
    static final String CSV_COLUMN_CONFIG_ID = "config_id";
    static final String CSV_COLUMN_PLANNER_TYPE = "planner";
    static final String CSV_COLUMN_PLANNING_TIME = "planning_time";
    static final String CSV_COLUMN_SPEECH_COST_CHARACTERS = "speech_cost_chars";
    static final String CSV_COLUMN_SPEECH_COST_SECONDS = "speech_cost_seconds";
    static final String CSV_COLUMN_SPEAKING_TIME_RELATIVE_TO_NAIVE = "speaking_time_relative_to_naive";
    static List<String> columnNames = new ArrayList<>();
    static {
        columnNames.add(CSV_COLUMN_QUERY_ID);
        columnNames.add(CSV_COLUMN_CONFIG_ID);
        columnNames.add(CSV_COLUMN_PLANNER_TYPE);
        columnNames.add(CSV_COLUMN_PLANNING_TIME);
        columnNames.add(CSV_COLUMN_SPEECH_COST_CHARACTERS);
        columnNames.add(CSV_COLUMN_SPEECH_COST_SECONDS);
        columnNames.add(CSV_COLUMN_SPEAKING_TIME_RELATIVE_TO_NAIVE);
        columnNames.addAll(TupleCollection.csvColumnNames());
        columnNames.addAll(ToleranceConfig.csvColumnNames());
    }

    static final String ANALYTICS_BASE_DIR = "analytics/";
    static String csvFileName = "analytics/csv_output.csv";

    static String[] testQueries = {
            "select model, dollars, pounds, inch_display from macbooks",
            "select restaurant, rating, price, cuisine from restaurants"
    };

    public static void main(String[] args) {
        List<Map<String, String>> testResults = new ArrayList<>();

        int queryId = 1;

        for (String query : testQueries) {
            TupleCollection tupleCollection = null;
            try {
                tupleCollection = DatabaseUtilities.executeQuery(query);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            Map<String, String> naiveMap = (testAndBuildCSV(queryId, 0, new NaiveVoicePlanner(), tupleCollection, 0));
            int naiveSpeechCostSeconds = Integer.parseInt(naiveMap.get(CSV_COLUMN_SPEECH_COST_SECONDS));
            testResults.add(naiveMap);


            int[] mSValues = { 1, 2, 3 };
            int[] mCValues = { 1, 2 };
            double[] mWValues = { 1.0, 1.5, 2.0 };

            int configId = 1;

            for (int i = 0; i < mSValues.length; i++) {
                int mS = mSValues[i];
                for (int j = 0; j < mCValues.length; j++) {
                    int mC = mCValues[j];
                    for (int k = 0; k < mWValues.length; k++) {
                        double mW = mWValues[k];

                        testResults.add(testAndBuildCSV(queryId, configId, new GreedyPlanner(mS, mW, mC), tupleCollection, naiveSpeechCostSeconds));
                        testResults.add(testAndBuildCSV(queryId, configId, new LinearProgrammingPlanner(mS, mW, mC), tupleCollection, naiveSpeechCostSeconds));
                        testResults.add(testAndBuildCSV(queryId, configId, new HybridPlanner(new TupleCoveringPruner(10), mS, mW, mC), tupleCollection, naiveSpeechCostSeconds));

                        configId++;
                    }
                }
            }
            queryId++;
        }

        String csvResult = csvMapToString(testResults);
        System.out.println(csvResult);
        writeTextToFile(csvFileName, csvResult);
    }

    public static Map<String, String> testAndBuildCSV(int queryId, int configId, VoicePlanner planner, TupleCollection tupleCollection, int naiveSpeechCostSeconds) {
        Map<String, String> csv = new HashMap<>();
        csv.putAll(tupleCollection.csvMap());
        csv.putAll(planner.getConfig().csvMap());
        csv.put(CSV_COLUMN_QUERY_ID, queryId + "");
        csv.put(CSV_COLUMN_CONFIG_ID, configId + "");
        csv.put(CSV_COLUMN_PLANNER_TYPE, planner.getPlannerName());

        // calculate planning time
        long startTime = System.currentTimeMillis();
        VoiceOutputPlan outputPlan = planner.plan(tupleCollection);
        long endTime = System.currentTimeMillis();
        double planningTime = (endTime - startTime) / 1000.0;
        csv.put(CSV_COLUMN_PLANNING_TIME, planningTime + "");

        int speechCostChars = outputPlan.toSpeechText(true).length();
        csv.put(CSV_COLUMN_SPEECH_COST_CHARACTERS, speechCostChars + "");

        // write the output to a txt file and to a wav file
        String fileNameBase = subdirectoryForQueryAndConfig(queryId, configId) + planner.getPlannerName();
        writeTextToFile(fileNameBase + ".txt", outputPlan.toSpeechText(true));
        String audioFileName = fileNameBase + ".wav";
        new WatsonVoiceGenerator().generateAndWriteToFile(outputPlan.toSpeechText(false), audioFileName);

        try {
            File file = new File(audioFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            long audioFileLength = file.length();
            int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            int speechCostSeconds = (int) (audioFileLength / (frameSize * frameRate));
            csv.put(CSV_COLUMN_SPEECH_COST_SECONDS, speechCostSeconds + "");
            if (planner instanceof NaiveVoicePlanner) {
                csv.put(CSV_COLUMN_SPEAKING_TIME_RELATIVE_TO_NAIVE, "1.0");
            } else {
                csv.put(CSV_COLUMN_SPEAKING_TIME_RELATIVE_TO_NAIVE, (((double) speechCostSeconds) / naiveSpeechCostSeconds) + "");
            }
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return csv;
    }

    public static String csvMapToString(List<Map<String, String>> csvList) {
        String header = "";
        Iterator<String> columns = columnNames.iterator();
        while (columns.hasNext()) {
            header += columns.next();
            header += columns.hasNext() ? "," : "\n";
        }

        String body = "";
        for (Map<String, String> csv : csvList) {
            columns = columnNames.iterator();
            while (columns.hasNext()) {
                String column = columns.next();
                body += csv.containsKey(column) ? csv.get(column) : "null";
                body += columns.hasNext() ? "," : "\n";
            }
        }

        return header + body;
    }

    public static String subdirectoryForQueryAndConfig(int queryId, int configId) {
        String s = ANALYTICS_BASE_DIR + "query_" + queryId + "/" + (configId == 0 ? "" : "config_" + configId + "/");
        File f = new File(s);
        f.mkdirs();
        return s;
    }

    public static void writeTextToFile(String fileName, String text) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(fileName);
            bw = new BufferedWriter(fw);
            bw.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
