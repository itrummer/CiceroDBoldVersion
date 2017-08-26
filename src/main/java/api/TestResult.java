package api;

import planning.PlanningResult;

public class TestResult {
    private final long id;
    private final PlanningResult result;

    public TestResult(long id, PlanningResult result) {
        this.id = id;
        this.result = result;
    }

    public long getId() {
        return id;
    }

    public PlanningResult getResult() {
        return result;
    }
}
