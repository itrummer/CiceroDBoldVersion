package planner.elements;

import db.Tuple;
import db.ValueAssignment;
import values.CategoricalValue;
import values.CategoricalValueAssignment;

import java.util.ArrayList;

/**
 * A set of value assignments
 */
public class Context {
    ArrayList<ValueAssignment> valueAssignments;
    ArrayList<CategoricalValueAssignment> categoricalValueAssignments;
    String cachedResult;

    public Context() {
        this.valueAssignments = new ArrayList<ValueAssignment>();
        this.categoricalValueAssignments = new ArrayList<CategoricalValueAssignment>();
        this.cachedResult = null;
    }

    public void addValueAssignment(ValueAssignment valueAssignment) {
        valueAssignments.add(valueAssignment);
    }

    public void addCategoricalValueAssignment(CategoricalValueAssignment valueAssignment) {
        categoricalValueAssignments.add(valueAssignment);
    }

    public ArrayList<ValueAssignment> getValueAssignments() {
        return valueAssignments;
    }

    /**
     * Determines if a given tuple matches this context. A Tuple matches a Context if it has the same ValueAssignments
     * for all ValueAssignments in this Context.
     * @param tuple The Tuple to check for a match
     * @return True if tuple matches this context, else false.
     */
    public boolean match(Tuple tuple) {
        return false;
    }

    public String toSpeechText() {
        if (cachedResult != null) {
            return cachedResult;
        }

        cachedResult = "";
        for (int i = 0; i < valueAssignments.size(); i++) {
            cachedResult += valueAssignments.get(i).toSpeechText();
            if (i < valueAssignments.size() - 1) {
                cachedResult += ", ";
            } else {
                cachedResult += ": ";
            }
        }
        return cachedResult;
    }
}
