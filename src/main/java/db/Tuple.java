package db;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a tuple in a database table
 */
public class Tuple {
    public HashMap<String, Object> valueAssignments;

    /**
     * Constructor of a Tuple from an ArrayList of ValueAssignments
     * @param valueAssignments
     */
    public Tuple(HashMap<String, Object> valueAssignments) {
        this.valueAssignments = valueAssignments;
    }

    /**
     * Constructor for a Tuple without ValueAssignments
     */
    public Tuple() {
        this.valueAssignments = new HashMap<String, Object>();
    }

    public HashMap<String, Object> getValueAssignments() {
        return valueAssignments;
    }

    /**
     * Returns the dimension of the tuple, which is the same as the number of values or the number of attributes
     */
    public int getDimension() {
        return valueAssignments.size();
    }

    /**
     * Adds a ValueAssignment to this Tuple.
     * @param column The attribute or column name for the value
     * @param value The value for the new value assignment
     */
    public void addValueAssignment(String column, Object value) {
        valueAssignments.put(column, value);
    }

    public Object valueForAttribute(String attribute) {
        return valueAssignments.get(attribute);
    }

    @Override
    public String toString() {
        String result = "";
        int count = 0;
        for (String column : valueAssignments.keySet()) {
            result += column + " : " + valueAssignments.get(column);
            count ++;
            if (count != valueAssignments.keySet().size()) {
                result += ", ";
            } else {
                result += ".";
            }
        }
        return result;
    }
}
