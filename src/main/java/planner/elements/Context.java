package planner.elements;

import db.Tuple;
import values.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A set of value assignments
 */
public class Context {
    HashMap<String, CategoricalValueDomain> categoricalValueAssignments;
    HashMap<String, NumericalValueDomain> numericalValueAssignments;
    String cachedResult;

    public Context() {
        this.categoricalValueAssignments = new HashMap<String, CategoricalValueDomain>();
        this.numericalValueAssignments = new HashMap<String, NumericalValueDomain>();
        this.cachedResult = null;
    }

    public void addCategoricalValueAssignment(String attribute, Value value) {
        if (categoricalValueAssignments.containsKey(attribute)) {
            categoricalValueAssignments.get(attribute).addValueToDomain(value);
        } else {
            categoricalValueAssignments.put(attribute, new CategoricalValueDomain(attribute, value));
        }
    }

    public void addCategoricalValueAssignments(String attribute, ArrayList<Value> valuesInDomain) {
        for (Value v : valuesInDomain) {
            addCategoricalValueAssignment(attribute, v);
        }
    }

    public void addNumericalValueAssignment(String attribute, Value lowerBound, Value upperBound) {
        numericalValueAssignments.put(attribute, new NumericalValueDomain(attribute, lowerBound, upperBound));
    }

    public boolean isAttributeFixed(String attribute) {
        return categoricalValueAssignments.containsKey(attribute) || numericalValueAssignments.containsKey(attribute);
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

        for (CategoricalValueDomain categoricalValueDomain : categoricalValueAssignments.values()) {
            cachedResult += categoricalValueDomain.toSpeechText() + ", ";
        }

        for (NumericalValueDomain numericalValueDomain : numericalValueAssignments.values()) {
            cachedResult += numericalValueDomain.toSpeechText() + ", ";
        }

        return cachedResult;
    }

    @Override
    public String toString() {
        // TODO: summarize data structure
        return super.toString();
    }
}
