package api;

import org.springframework.web.bind.annotation.*;
import planning.PlanningManager;
import planning.PlanningResult;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class TestController {
    private final AtomicLong counter = new AtomicLong();
    private final PlanningManager planningManager = new PlanningManager();

    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public TestResult newTest(@RequestBody TestInstance testInstance) throws Exception {
        PlanningResult result = planningManager.buildPlan(testInstance.getPlanner(), testInstance.getData(), testInstance.getConfig());
        return new TestResult(counter.incrementAndGet(), result);
    }

}
