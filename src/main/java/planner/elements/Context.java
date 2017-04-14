package planner.elements;

import db.Tuple;
import values.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A set of value assignments
 */
public class Context {
    HashMap<String, CategoricalValueAssignment> categoricalValueAssignments;
    ArrayList<NumericalValueAssignment> numericalValueAssignments;
    String cachedResult;

    public Context() {
        this.categoricalValueAssignments = new HashMap<String, CategoricalValueAssignment>();
        this.numericalValueAssignments = new ArrayList<NumericalValueAssignment>();
        this.cachedResult = null;
    }

    public void addCategoricalValueAssignment(String attribute, Value value) {
        if (categoricalValueAssignments.containsKey(attribute)) {
            categoricalValueAssignments.get(attribute).addValueToDomain(value);
        } else {
            categoricalValueAssignments.put(attribute, new CategoricalValueAssignment(attribute, value));
        }
    }

    public void addCategoricalValueAssignments(String attribute, ArrayList<Value> valuesInDomain) {
        for (Value v : valuesInDomain) {
            addCategoricalValueAssignment(attribute, v);
        }
    }

    public void addNumericalValueAssignment(String attribute, Value lowerBound, Value upperBound) {
        numericalValueAssignments.add(new NumericalValueAssignment(attribute, lowerBound, upperBound));
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


        cachedResult = "TODO...Context";
        return cachedResult;
//        for (int i = 0; i < valueAssignments.size(); i++) {
//            cachedResult += valueAssignments.get(i).toSpeechText();
//            if (i < valueAssignments.size() - 1) {
//                cachedResult += ", ";
//            } else {
//                cachedResult += ": ";
//            }
//        }
//        return cachedResult;
    }

    @Override
    public String toString() {
        // TODO: summarize data structure
        return super.toString();
    }
}
