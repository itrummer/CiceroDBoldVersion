package planner.elements;

import planner.Speakable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a tuple in a database table
 */
public class Tuple implements Speakable {
    ArrayList<String> attributes;
    HashMap<String, Value> valueAssignments;
    String primaryKey;

    /**
     * Constructor for a Tuple
     */
    public Tuple(ArrayList<String> attributes) {
        this.attributes = attributes;
        this.valueAssignments = new HashMap<String, Value>();
    }

    /**
     * Returns this Tuples value assignments. A value assignment is a mapping from a String attribute to a Value
     */
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

    public void setPrimaryKey(String s) {
        primaryKey = s;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    /**
     * Retrieves the value for the specified attribute
     */
    public Value valueForAttribute(String attribute) {
        return valueAssignments.get(attribute);
    }

    public String toSpeechText(boolean inLongForm) {
        String result = "";
        for (int i = 0; i < attributes.size(); i++) {
            String attribute = attributes.get(i);
            result += attribute + " " + valueAssignments.get(attribute).toSpeechText(inLongForm);
            if (i < attributes.size()-1) {
                result += ", ";
            }
        }
        return result;
    }
}
