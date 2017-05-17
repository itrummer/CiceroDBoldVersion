package planner;

/**
 */
public class ToleranceConfig {
    Integer mS;
    Double mW;
    Integer mC;

    public ToleranceConfig(int mS, double mW, int mC) {
        this.mS = mS;
        this.mW = mW;
        this.mC = mC;
    }

    public ToleranceConfig() {
        this.mS = null;
        this.mW = null;
        this.mC = null;
    }

    public String getCSV() {
        return mS + "," + mW + "," + mC;
    }

    public static String getCSVHeader() {
        return "mS,mW,mC";
    }

}
