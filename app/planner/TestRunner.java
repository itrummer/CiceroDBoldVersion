package planner;

import planner.elements.TupleCollection;
import planner.greedy.GreedyPlanner;
import planner.hybrid.HybridPlanner;
import planner.hybrid.TupleCoveringPruner;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import util.DatabaseUtilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;

public class TestRunner {
    static String csvFileName = "csv_output.csv";
    static String[] testQueries = {
            "select model, dollars, pounds, inch_display from macbooks",
            "select * from restaurants"
    };

    public static void main(String[] args) {
        String csvResult = "query," + TupleCollection.getCSVHeader() + ",type,planning_time,speech_cost_in_chars," + ToleranceConfig.getCSVHeader() + "\n";

        for (String query : testQueries) {
            TupleCollection tupleCollection = null;
            try {
                tupleCollection = DatabaseUtilities.executeQuery(query);
            } catch (SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }

            VoicePlanner planner = new NaiveVoicePlanner();
            csvResult += computeCSV(query, planner, tupleCollection) + "\n";

            int[] mSValues = { 1, 2 };
            int[] mCValues = { 1, 2 };
            double[] mWValues = { 1.0, 2.0 };

            for (int i = 0; i < mSValues.length; i++) {
                int mS = mSValues[i];
                for (int j = 0; j < mCValues.length; j++) {
                    int mC = mCValues[j];
                    for (int k = 0; k < mWValues.length; k++) {
                        double mW = mWValues[k];

                        // Compute CSV for all planner types

                        planner = new GreedyPlanner(mS, mW, mC);
                        csvResult += computeCSV(query, planner, tupleCollection) + "\n";

                        planner = new LinearProgrammingPlanner(mS, mW, mC);
                        csvResult += computeCSV(query, planner, tupleCollection) + "\n";

                        planner = new HybridPlanner(new TupleCoveringPruner(10), mS, mW, mC);
                        csvResult += computeCSV(query, planner, tupleCollection) + "\n";
                    }
                }
            }
        }

        writeCSVToFile(csvResult);
    }

    public static String computeCSV(String query, VoicePlanner planner, TupleCollection tupleCollection) {
        long startTime = System.currentTimeMillis();
        VoiceOutputPlan outputPlan = planner.plan(tupleCollection);
        long endTime = System.currentTimeMillis();
        double planningTime = (endTime - startTime) / 1000.0;

        int speechLength = outputPlan.toSpeechText(true).length();

        // TODO: also generate actual audio file

        return "\"" + query + "\"," + tupleCollection.csvDescription() + "," + planner.getPlannerName() + "," + planningTime + "," +
                speechLength + "," + planner.getConfig().getCSV();
    }

    public static void writeCSVToFile(String csv) {
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            fw = new FileWriter(csvFileName);
            bw = new BufferedWriter(fw);
            bw.write(csv);
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
