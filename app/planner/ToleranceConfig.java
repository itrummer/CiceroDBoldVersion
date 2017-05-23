package planner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ToleranceConfig {
    static final String CSV_COLUMN_MS = "mS";
    static final String CSV_COLUMN_MW = "mW";
    static final String CSV_COLUMN_MC = "mC";

    Integer mS;
    Double mW;
    Integer mC;

    public ToleranceConfig(int mS, double mW, int mC) {
        this.mS = mS;
        this.mW = mW;
        this.mC = mC;
    }

    public static List<String> csvColumnNames() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add(CSV_COLUMN_MS);
        columnNames.add(CSV_COLUMN_MW);
        columnNames.add(CSV_COLUMN_MC);
        return columnNames;
    }

    public Map<String, String> csvMap() {
        Map<String, String> csv = new HashMap<>();
        csv.put(CSV_COLUMN_MS, mS != null ? mS.toString() : "null");
        csv.put(CSV_COLUMN_MW, mW != null ? mW.toString() : "null");
        csv.put(CSV_COLUMN_MC, mC != null ? mC.toString() : "null");
        return csv;
    }

    public int getMaxContextSize() {
        return mS;
    }

    public double getMaxNumericalDomainWidth() {
        return mW;
    }

    public int getMaxCategoricalDomainSize() {
        return mC;
    }

}
