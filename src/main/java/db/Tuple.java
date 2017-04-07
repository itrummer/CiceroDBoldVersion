package db;

import java.util.ArrayList;

/**
 * Represents a tuple in a database table
 */
public class Tuple {
    public ArrayList<ValueAssignment> valueAssignments;

    /**
     * Constructor of a Tuple from an ArrayList of ValueAssignments
     * @param valueAssignments
     */
    public Tuple(ArrayList<ValueAssignment> valueAssignments) {
        this.valueAssignments = valueAssignments;
    }

    /**
     * Constructor for a Tuple without ValueAssignments
     */
    public Tuple() {
        this.valueAssignments = new ArrayList<ValueAssignment>();
    }

    public ArrayList<ValueAssignment> getValueAssignments() {
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
        valueAssignments.add(new ValueAssignment(column, value));
    }

    @Override
    public String toString() {
        String result = "";
        for (ValueAssignment valueAssignment : getValueAssignments()) {
            result += valueAssignment.toString() + ", ";
        }
        return result;
    }
}
