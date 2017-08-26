package api;

import planning.config.Config;

public class TestInstance {
    String csvHeader;
    String csvBody;
    String algorithm;
    Config config;

    public String getCsvHeader() {
        return csvHeader;
    }

    public void setCsvHeader(String csvHeader) {
        this.csvHeader = csvHeader;
    }

    public String getCsvBody() {
        return csvBody;
    }

    public void setCsvBody(String csvBody) {
        this.csvBody = csvBody;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
