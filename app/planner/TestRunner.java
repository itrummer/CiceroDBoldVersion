package planner;

import planner.elements.TupleCollection;
import planner.greedy.GreedyPlanner;
import planner.hybrid.HybridPlanner;
import planner.hybrid.TupleCoveringPruner;
import planner.linear.LinearProgrammingPlanner;
import planner.naive.NaiveVoicePlanner;
import util.DatabaseUtilities;

import java.util.ArrayList;
import java.util.List;


public class TestRunner {
    List<VoicePlanner> planners;
    static String[] testQueries = {
            "select model, dollars, pounds, inch_display from macbooks;",
            "select * from restaurants;"
    };


    public TestRunner() {
        planners = new ArrayList<>();

        // Naive planners
        planners.add(new NaiveVoicePlanner());

        int[] mSValues = { 1, 2, 3 };
        int[] mCValues = { 1, 2 };
        double[] mWValues = { 1.0, 1.5, 2.0 };

        for (int i = 0; i < mSValues.length; i++) {
            int mS = mSValues[i];
            for (int j = 0; j < mCValues.length; j++) {
                int mC = mCValues[j];
                for (int k = 0; k < mWValues.length; k++) {
                    double mW = mWValues[k];
                    planners.add(new GreedyPlanner(mS, mW, mC));
//                    planners.add(new LinearProgrammingPlanner(mS, mW, mC));
                    planners.add(new HybridPlanner(new TupleCoveringPruner(10), mS, mW, mC));
                }
            }
        }
    }

    public String runTests() {
        String result = "";

        for (int i = 0; i < testQueries.length; i++) {
            String query = testQueries[i];
            TupleCollection tupleCollection;
            try {
                tupleCollection = DatabaseUtilities.executeQuery(query);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            for (VoicePlanner planner : planners) {
                long startTime = System.nanoTime();
                planner.plan(tupleCollection);
                long endTime = System.nanoTime();
                String thisResult = planner.getClass().getSimpleName() + " test: " + (endTime - startTime);
                result += thisResult + "\n";
            }
        }

        return result;
    }

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        System.out.println(runner.runTests());
    }

}
