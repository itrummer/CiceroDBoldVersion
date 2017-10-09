package api;

import data.CSVConnector;
import data.SQLConnector;
import planning.VoicePlanner;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.greedy.GreedyPlanner;
import planning.planners.hybrid.HybridPlanner;
import planning.planners.hybrid.TupleCoveringPruner;
import planning.planners.linear.LinearProgrammingPlanner;
import planning.planners.naive.NaiveVoicePlanner;

public class TestInstance {
    final static CSVConnector csvConnector = new CSVConnector();
    final static SQLConnector sqlConnector = new SQLConnector();

    String sqlQuery;
    String csvHeader;
    String csvBody;
    String algorithm;
    Config config;
    String tuplesClassName;

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getTuplesClassName() {
        return tuplesClassName;
    }

    public void setTuplesClassName(String tuplesClassName) {
        this.tuplesClassName = tuplesClassName;
    }

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

    public TupleCollection getData() throws Exception {
        if (sqlQuery != null) {
            return sqlConnector.buildTupleCollectionFromQuery(sqlQuery, tuplesClassName);
        } else {
            return csvConnector.buildTupleCollectionFromCSV(tuplesClassName, csvBody, csvHeader.split(","));
        }
    }

    public VoicePlanner getPlanner() throws Exception {
        switch (algorithm) {
            case "naive":
                return new NaiveVoicePlanner();
            case "hybrid":
                return new HybridPlanner(new TupleCoveringPruner(10));
            case "greedy":
                return new GreedyPlanner();
            case "linear":
                return new LinearProgrammingPlanner();
        }
        throw new InvalidAlgorithmException(algorithm);
    }

    public class InvalidAlgorithmException extends Exception {
        public InvalidAlgorithmException(String algorithm) {
            super("Invalid algorithm: " + algorithm);
        }
    }
}
