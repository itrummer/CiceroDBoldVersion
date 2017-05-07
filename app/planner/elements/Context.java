package planner.elements;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A set of value assignments
 */
public class Context {
    HashMap<String, CategoricalValueDomain> categoricalValueAssignments;
    HashMap<String, NumericalValueDomain> numericalValueAssignments;
    String cachedShortResult;
    String cachedLongFormResult;

    public Context() {
        this.categoricalValueAssignments = new HashMap<String, CategoricalValueDomain>();
        this.numericalValueAssignments = new HashMap<String, NumericalValueDomain>();
        this.cachedShortResult = null;
        this.cachedLongFormResult = null;
    }

    public Context(Context otherContext) {
        this();
        this.categoricalValueAssignments.putAll(otherContext.categoricalValueAssignments);
        this.numericalValueAssignments.putAll(otherContext.numericalValueAssignments);
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

    public void addDomainAssignment(ValueDomain valueDomain) {
        String attribute = valueDomain.getAttribute();
        if (valueDomain.isCategorical()) {
            categoricalValueAssignments.put(attribute, (CategoricalValueDomain) valueDomain);
        } else if (valueDomain.isNumerical()) {
            numericalValueAssignments.put(attribute, (NumericalValueDomain) valueDomain);
        }
    }

    /**
     * Determines if a Tuple matches this Context. A Tuple matches a Context if for all attributes in
     * which the Context fixes a domain, the Tuple has a value for that attribute that is within
     * the fixed domain.
     * @param t The Tuple to consider
     * @return whether the given Tuple matches this Context
     */
    public boolean matches(Tuple t) {
        for (String attribute : categoricalValueAssignments.keySet()) {
            ValueDomain domain = categoricalValueAssignments.get(attribute);
            Value vT = t.valueForAttribute(attribute);
            if (!domain.contains(vT)) {
                return false;
            }
        }

        for (String attribute : numericalValueAssignments.keySet()) {
            ValueDomain domain = numericalValueAssignments.get(attribute);
            Value vT = t.valueForAttribute(attribute);
            if (!domain.contains(vT)) {
                return false;
            }
        }

        return true;
    }

    public String toSpeechText(boolean inLongForm) {
        if (cachedLongFormResult != null && inLongForm) {
            return cachedLongFormResult;
        } else if (cachedShortResult != null && !inLongForm) {
            return cachedShortResult;
        }

        String cachedResult = "";

        ArrayList<String> parsed = new ArrayList<>();

        for (CategoricalValueDomain categoricalValueDomain : categoricalValueAssignments.values()) {
            parsed.add(categoricalValueDomain.toSpeechText(inLongForm));
        }

        for (NumericalValueDomain numericalValueDomain : numericalValueAssignments.values()) {
            parsed.add(numericalValueDomain.toSpeechText(inLongForm));
        }

        if (parsed.size() == 1) {
            cachedResult = parsed.get(0);
        } else if (parsed.size() == 2) {
            cachedResult += parsed.get(0) + " and " + parsed.get(1);
        } else if (parsed.size() > 2) {
            cachedResult += parsed.get(0);
            for (int i = 1; i < parsed.size() - 1; i++) {
                cachedResult += ", " + parsed.get(i);
            }
            cachedResult += ", and " + parsed.get(parsed.size()-1);
        }

        if (inLongForm) {
            cachedLongFormResult = cachedResult;
        } else {
            cachedShortResult = cachedResult;
        }

        return cachedResult;
    }

    @Override
    public String toString() {
        return "Context: " + (categoricalValueAssignments.isEmpty() ? "" : categoricalValueAssignments.values() + " and ") + (numericalValueAssignments.isEmpty() ? "" : numericalValueAssignments.values());
    }
}
