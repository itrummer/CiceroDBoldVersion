package planner;

import db.Tuple;
import db.RowCollection;

import java.util.ArrayList;

/**
 * A naive implementation of a voice plan. Lists all results in a query as individual tuples.
 */
public class NaiveVoicePlanner extends VoicePlanner {

    public static String EMPTY_ROW_COLLECTION_PLAN = "No results.";

    String result;

    @Override
    public void plan(RowCollection rowCollection) {
        result = "";
        ArrayList<Tuple> rows = rowCollection.getRows();
        if (rows.size() == 0) {
            result = EMPTY_ROW_COLLECTION_PLAN;
        } else {
            for (int i = 0; i < rows.size(); i++) {
                result += "Item " + (i + 1) + ": ";
                plan(rows.get(i));
            }
        }
    }

    @Override
    public void plan(Tuple row) {
        ArrayList<Object> values = row.getValues();
        for (int i = 0; i < values.size(); i++) {
            // TODO: incorporate column names in result
            result += values.get(i);
            if (i != values.size() - 1) {
                result += " ";
            }
        }
        result += ". ";
    }

    public String getResult() {
        if (result == null) {
            return "No rowCollection visited. No result to display";
        }
        return result;
    }
}
