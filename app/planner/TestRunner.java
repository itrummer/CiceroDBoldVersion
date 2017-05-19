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

public class TestRunner {
    static final String ANALYTICS_BASE_DIR = "analytics/";
    static String csvFileName = "analytics/csv_output.csv";
    static String[] testQueries = {
            "select model, dollars, pounds, inch_display from macbooks",
            "select * from restaurants"
    };
    static WatsonVoiceGenerator voiceGenerator = new WatsonVoiceGenerator();

    public static void main(String[] args) {
        String csvResult = "query_id,config_id,query," + TupleCollection.getCSVHeader() + ",type,planning_time,speech_cost_in_chars,speech_cost_in_seconds," + ToleranceConfig.getCSVHeader() + "\n";

        int queryId = 1;

        for (String query : testQueries) {
            TupleCollection tupleCollection = null;
            try {
                tupleCollection = DatabaseUtilities.executeQuery(query);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            csvResult += computeCSV(queryId, 0, query, new NaiveVoicePlanner(), tupleCollection) + "\n";

            int[] mSValues = { 1, 2 };
            int[] mCValues = { 1, 2 };
            double[] mWValues = { 1.0, 2.0 };

            int configId = 1;

            for (int i = 0; i < mSValues.length; i++) {
                int mS = mSValues[i];
                for (int j = 0; j < mCValues.length; j++) {
                    int mC = mCValues[j];
                    for (int k = 0; k < mWValues.length; k++) {
                        double mW = mWValues[k];

                        csvResult += computeCSV(queryId, configId, query, new GreedyPlanner(mS, mW, mC), tupleCollection) + "\n";
                        csvResult += computeCSV(queryId, configId, query, new LinearProgrammingPlanner(mS, mW, mC), tupleCollection) + "\n";
                        csvResult += computeCSV(queryId, configId, query, new HybridPlanner(new TupleCoveringPruner(10), mS, mW, mC), tupleCollection) + "\n";

                        configId++;
                    }
                }
            }
            queryId++;
        }

        writeTextToFile(csvFileName, csvResult);
    }

    public static String computeCSV(int queryId, int configId, String query, VoicePlanner planner, TupleCollection tupleCollection) {
        // measure planning time
        long startTime = System.currentTimeMillis();
        VoiceOutputPlan outputPlan = planner.plan(tupleCollection);
        long endTime = System.currentTimeMillis();
        double planningTime = (endTime - startTime) / 1000.0;

        int speechLength = outputPlan.toSpeechText(true).length();

        // write the output to a txt file and to a wav file
        String fileNameBase = subdirectoryForQueryAndConfig(queryId, configId) + planner.getPlannerName();
        writeTextToFile(fileNameBase + ".txt", outputPlan.toSpeechText(true));
        String audioFileName = fileNameBase + ".wav";
        voiceGenerator.generateAndWriteToFile(outputPlan.toSpeechText(false), audioFileName);

        int durationInSeconds = -1;
        try {
            File file = new File(audioFileName);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioInputStream.getFormat();
            long audioFileLength = file.length();
            int frameSize = format.getFrameSize();
            float frameRate = format.getFrameRate();
            durationInSeconds = (int) (audioFileLength / (frameSize * frameRate));
            audioInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return queryId + "," + configId + ",\"" + query + "\"," + tupleCollection.csvDescription() + "," + planner.getPlannerName() + "," + planningTime + "," +
                speechLength + "," + durationInSeconds + "," + planner.getConfig().getCSV();
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
