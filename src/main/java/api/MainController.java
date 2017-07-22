package api;

import data.SQLConnector;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import planning.PlanningManager;
import planning.PlanningResult;
import planning.config.Config;
import planning.elements.TupleCollection;
import planning.planners.naive.NaiveVoicePlanner;

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
        SQLConnector sqlConnector = new SQLConnector();
        TupleCollection tuples = sqlConnector.buildTupleCollectionFromQuery("select * from restaurants limit 10");
        return planningManager.buildPlan(planner, tuples, new Config());
    }
}
