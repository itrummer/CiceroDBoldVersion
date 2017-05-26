package planner.elements;

import planner.Speakable;

import java.util.*;

/**
 */
public class CategoricalValueDomain extends ValueDomain implements Speakable {
    Set<Value> domainValues;
    String shortFormCachedResult;
    String longFormCachedResult;

    public CategoricalValueDomain(String attribute, List<Value> domainValues) {
        this.attribute = attribute;
        this.domainValues = new HashSet<>(domainValues);
        this.shortFormCachedResult = null;
        this.longFormCachedResult = null;
    }

    public CategoricalValueDomain(String attribute) {
        this(attribute, new ArrayList<>());
    }

    public CategoricalValueDomain(String attribute, Value firstValue) {
        this(attribute);
        addValueToDomain(firstValue);
    }

    public void addValueToDomain(Value value) {
        domainValues.add(value);
    }

    /**
     * Determines if a Value is within this CategoricalValueDomain. A Value is within
     * this domain if it is equal to one of the categorical Values that this domain fixes.
     * @param v The Value to check.
     * @return A boolean Value indicating if v is within this CategoricalValueDomain
     */
    @Override
    public boolean contains(Value v) {
        return (domainValues.contains(v));
    }

    @Override
    public boolean isCategorical() {
        return true;
    }

    @Override
    public boolean isNumerical() {
        return false;
    }

    @Override
    public String toSpeechText(boolean inLongForm) {
        if (inLongForm && longFormCachedResult != null) {
            return longFormCachedResult;
        } else if (!inLongForm && shortFormCachedResult != null) {
            return shortFormCachedResult;
        }

        Value[] domainList = domainValues.toArray(new Value[0]);

        StringBuilder result = new StringBuilder("");

        if (domainList.length == 1) {
            // example: "category Italian"
            result.append(domainList[0].toSpeechText(inLongForm) + " " + attribute);
        } else if (domainValues.size() == 2) {
            // example: "category Italian or American"
            result.append(domainList[0].toSpeechText(inLongForm));
            result.append(" or ");
            result.append(domainList[1].toSpeechText(inLongForm));
            result.append(" ");
            result.append(attribute);
        } else {
            // example: "category Italian, American, or Pub Food"
            result.append(domainList[0].toSpeechText(inLongForm));
            result.append(" ");
            result.append(attribute);
            for (int i = 1; i < domainList.length; i++) {
                result.append(", ");
                result.append(domainList[i].toSpeechText(inLongForm));
            }
        }

        if (inLongForm) {
            longFormCachedResult = result.toString();
            return longFormCachedResult;
        } else {
            shortFormCachedResult = result.toString();
            return shortFormCachedResult;
        }
    }

    @Override
    public String toString() {
        return attribute + " : " + domainValues;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CategoricalValueDomain)) {
            return false;
        }

        CategoricalValueDomain otherDomain = (CategoricalValueDomain) obj;
        if (domainValues.size() != otherDomain.domainValues.size()) {
            return false;
        }

        for (Value v : domainValues) {
            if (!otherDomain.contains(v)) {
                return false;
            }
        }


        return true;
    }

    @Override
    public int hashCode() {
        int total = 0;
        for (Value v : domainValues) {
            total += v.hashCode();
        }
        return total;
    }
}
