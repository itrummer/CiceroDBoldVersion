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

    public String toSpeechText() {
        if (cachedResult != null) {
            return cachedResult;
        }

        cachedResult = "";

        ArrayList<String> parsed = new ArrayList<String>();

        for (CategoricalValueDomain categoricalValueDomain : categoricalValueAssignments.values()) {
            parsed.add(categoricalValueDomain.toSpeechText());
        }

        for (NumericalValueDomain numericalValueDomain : numericalValueAssignments.values()) {
            parsed.add(numericalValueDomain.toSpeechText());
        }

        if (parsed.size() == 1) {
            cachedResult = parsed.get(0);
        } else if (parsed.size() == 2) {
            cachedResult += parsed.get(0) + " and " + parsed.get(1);
        } else if (parsed.size() > 2) {
            cachedResult += parsed.get(0);
            for (int i = 1; i < parsed.size(); i++) {
                cachedResult += ", " + parsed.get(i);
            }
        }
        return cachedResult;
    }

    @Override
    public String toString() {
        // TODO: summarize data structure
        return super.toString();
    }
}
