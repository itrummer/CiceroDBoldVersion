package util;

import planner.PlanningResult;
import planner.VoicePlanner;
import planner.elements.TupleCollection;

import java.util.*;

public class CSVBuilder {
    static String[] defaultColumnNames = {
            "test number",
            "test case name",
            "planner",
            "tuple count",
            "average entropy",
            "max context size",
            "max numerical domain width",
            "max categorical domain size",
            "speech cost (characters)",
            "speech cost relative to naive",
            "average planning time (milliseconds)",
            "execution count",
    };

    List<String> csvColumnNames;
    List<Map<String, String>> csvLines;
    int testNumber;

    public CSVBuilder() {
        this.csvColumnNames = new ArrayList<>();
        this.csvLines = new ArrayList<>();
        this.testNumber = 0;

        for (String c : defaultColumnNames) {
            csvColumnNames.add(c);
        }
    }

    public void addTestResult(VoicePlanner planner, PlanningResult result, PlanningResult naiveResult, TupleCollection tuples, String testCaseName) {
        Map<String, String> testCSV = new HashMap<>();
        testCSV.put("test number", testNumber++ + "");
        testCSV.put("test case name", testCaseName);
        testCSV.put("planner", planner.getPlannerName());
        testCSV.put("tuple count", tuples.tupleCount() + "");
        testCSV.put("average entropy", tuples.entropy(planner.getConfig().getMaxNumericalDomainWidth()) + "");
        testCSV.put("max context size", planner.getConfig().getMaxContextSize() + "");
        testCSV.put("max numerical domain width", planner.getConfig().getMaxNumericalDomainWidth() + "");
        testCSV.put("max categorical domain size", planner.getConfig().getMaxCategoricalDomainSize() + "");
        int speechCost = result.getPlan().toSpeechText(true).length();
        testCSV.put("speech cost (characters)", speechCost + "");
        int naiveCost = naiveResult.getPlan().toSpeechText(true).length();
        testCSV.put("speech cost relative to naive", String.format("%.4f", ((double) speechCost) / naiveCost) + "");
        testCSV.put("average planning time (milliseconds)", result.getAverageExecutionTimeMillis() + "");
        testCSV.put("execution count", result.getExecutionCount() + "");
        csvLines.add(testCSV);
    }

    private String csvHeader() {
        String header = "";
        Iterator<String> columns = csvColumnNames.iterator();
        while (columns.hasNext()) {
            header += columns.next();
            header += columns.hasNext() ? "," : "";
        }
        return header;
    }

    private String csvBody() {
        StringBuilder body = new StringBuilder("");
        for (Map<String, String> csv : csvLines) {
            Iterator<String> columns = csvColumnNames.iterator();
            while (columns.hasNext()) {
                String column = columns.next();
                body.append(csv.getOrDefault(column, ""));
                body.append(columns.hasNext() ? "," : "\n");
            }
        }
        return body.toString();
    }

    public String getCSVString() {
        return csvHeader() + "\n" + csvBody();
    }

}
