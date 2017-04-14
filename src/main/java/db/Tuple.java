package db;

import planner.Speakable;
import values.Value;
import java.util.HashMap;

/**
 * Represents a tuple in a database table
 */
public class Tuple implements Speakable {
    HashMap<String, Value> valueAssignments;

    /**
     * Constructor of a Tuple from an ArrayList of ValueAssignments
     * @param valueAssignments
     */
    public Tuple(HashMap<String, Value> valueAssignments) {
        this.valueAssignments = valueAssignments;
    }

    /**
     * Constructor for a Tuple without ValueAssignments
     */
    public Tuple() {
        this.valueAssignments = new HashMap<String, Value>();
    }

    public HashMap<String, Value> getValueAssignments() {
        return valueAssignments;
    }

    /**
     * Adds a ValueAssignment to this Tuple.
     * @param column The attribute or column name for the value
     * @param value The value for the new value assignment
     */
    public void addValueAssignment(String column, Value value) {
        valueAssignments.put(column, value);
    }

    public Value valueForAttribute(String attribute) {
        return valueAssignments.get(attribute);
    }

    public String toSpeechText() {
        String result = "";
        int count = 0;
        for (String column : valueAssignments.keySet()) {
            result += column + " : " + valueAssignments.get(column).toSpeechText();
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
