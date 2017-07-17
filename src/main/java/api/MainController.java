package api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import planning.PlanningManager;
import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.naive.NaiveVoicePlanner;
import util.DatabaseUtilities;

@RestController
public class MainController {

    @RequestMapping("/")
    public String index() {
        return "Welcome to CiceroDB";
    }

    @RequestMapping("/naive")
    public PlanningResult naive() throws Exception {
        PlanningManager planningManager = new PlanningManager();
        NaiveVoicePlanner planner = new NaiveVoicePlanner();
        TupleCollection tuples = DatabaseUtilities.executeQuery("select * from restaurants limit 10");
        return planningManager.buildPlan(planner, tuples, new Config());
    }
}
