package db;

import java.util.ArrayList;

/**
 * Represents a tuple in a database table
 */
public class Tuple {

    public ArrayList<Object> values;

    public Tuple(ArrayList<Object> values) {
        this.values = values;
    }

    public ArrayList<Object> getValues() {
        return values;
    }

    @Override
    public String toString() {
        String result = "";
        for (Object value : getValues()) {
            result += value.toString() + "\t";
        }
        return result;
    }
}
