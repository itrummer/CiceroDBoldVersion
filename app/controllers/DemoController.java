package controllers;

import models.TestQuery;
import planner.NaiveVoicePlanner;
import planner.elements.TupleCollection;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;

import util.DatabaseUtilities;
import views.html.*;

import javax.inject.Inject;

public class DemoController extends Controller {

    private FormFactory formFactory;

    @Inject
    public DemoController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    public Result index() {
        Form<TestQuery> testQueryForm = formFactory.form(TestQuery.class);
        return ok(demo.render(testQueryForm));
    }

    public Result execute() {
        Form<TestQuery> testQueryForm = formFactory.form(TestQuery.class).bindFromRequest();

        if (testQueryForm.hasErrors()) {
            return badRequest();
        }

        String query = testQueryForm.data().get("query");

        String result = "";
        try {
            TupleCollection tupleCollection = DatabaseUtilities.executeQuery(query);
            result = new NaiveVoicePlanner().plan(tupleCollection).toSpeechText(false);
        } catch (Exception e) {
            result = "error while executing query";
        }

        return ok(results.render(query, result));
    }

}
