package planner.elements;

import planner.Speakable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A set of value assignments
 */
public class Context implements Speakable {
    Map<String, ValueDomain> valueDomains;
    String cachedShortResult;
    String cachedLongFormResult;

    public Context() {
        this.valueDomains = new HashMap<>();
        this.cachedShortResult = null;
        this.cachedLongFormResult = null;
    }

    public Context(Context otherContext) {
        this();
        this.valueDomains.putAll(otherContext.valueDomains);
    }

    public void addCategoricalValueAssignment(String attribute, Value value) {
        if (valueDomains.containsKey(attribute)) {
            CategoricalValueDomain valueDomain = (CategoricalValueDomain) valueDomains.get(attribute);
            valueDomain.addValueToDomain(value);
        } else {
            valueDomains.put(attribute, new CategoricalValueDomain(attribute, value));
        }
    }

    public void addCategoricalValueAssignments(String attribute, ArrayList<Value> valuesInDomain) {
        for (Value v : valuesInDomain) {
            addCategoricalValueAssignment(attribute, v);
        }
    }

    public void addNumericalValueAssignment(String attribute, Value lowerBound, Value upperBound) {
        valueDomains.put(attribute, new NumericalValueDomain(attribute, lowerBound, upperBound));
    }

    public boolean isAttributeFixed(String attribute) {
        return valueDomains.containsKey(attribute);
    }

    public void addDomainAssignment(ValueDomain valueDomain) {
        String attribute = valueDomain.getAttribute();
        valueDomains.put(attribute, valueDomain);
    }

    /**
     * Determines if a Tuple matches this Context. A Tuple matches a Context if for all attributes in
     * which the Context fixes a domain, the Tuple has a value for that attribute that is within
     * the fixed domain.
     * @param t The Tuple to consider
     * @return whether the given Tuple matches this Context
     */
    public boolean matches(Tuple t) {
        for (String attribute : valueDomains.keySet()) {
            ValueDomain domain = valueDomains.get(attribute);
            Value vT = t.valueForAttribute(attribute);
            if (!domain.contains(vT)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toSpeechText(boolean inLongForm) {
        if (cachedLongFormResult != null && inLongForm) {
            return cachedLongFormResult;
        } else if (cachedShortResult != null && !inLongForm) {
            return cachedShortResult;
        }

        StringBuilder result = new StringBuilder("");

        List<String> parsed = new ArrayList<>();

        for (ValueDomain domain : valueDomains.values()) {
            parsed.add(domain.toSpeechText(inLongForm));
        }

        if (parsed.size() == 1) {
            result.append(parsed.get(0));
        } else if (parsed.size() == 2) {
            result.append(parsed.get(0) + " and " + parsed.get(1));
        } else if (parsed.size() > 2) {
            result.append(parsed.get(0));
            for (int i = 1; i < parsed.size() - 1; i++) {
                result.append(", " + parsed.get(i));
            }
            result.append(", and " + parsed.get(parsed.size()-1));
        }

        if (inLongForm) {
            cachedLongFormResult = result.toString();
        } else {
            cachedShortResult = result.toString();
        }

        return inLongForm ? cachedLongFormResult : cachedShortResult;
    }

    @Override
    public String toString() {
        return "Context: " + valueDomains;
    }
}
