package planner.elements;

import planner.Speakable;

import java.util.ArrayList;

/**
 */
public class CategoricalValueDomain extends ValueDomain implements Speakable {
    ArrayList<Value> domainValues;

    public CategoricalValueDomain(String attribute, ArrayList<Value> domainValues) {
        this.attribute = attribute;
        this.domainValues = domainValues;
    }

    public CategoricalValueDomain(String attribute) {
        this(attribute, new ArrayList<Value>());
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
        for (Value vInDomain : domainValues) {
            if (vInDomain.equals(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCategorical() {
        return true;
    }

    @Override
    public boolean isNumerical() {
        return false;
    }

    public String toSpeechText(boolean inLongForm) {
        if (domainValues.size() == 1) {
            // example: "category Italian"
            return domainValues.get(0).toSpeechText(inLongForm) + " " + attribute;
        }

        // example: "category Italian or American"
        if (domainValues.size() == 2) {
            return domainValues.get(0).toSpeechText(inLongForm) + " or " + domainValues.get(1).toSpeechText(inLongForm) + " " + attribute;
        }

        // example: "category Italian, American, or Pub Food"
        String result = domainValues.get(0).toSpeechText(inLongForm) + " " + attribute;
        for (int i = 1; i < domainValues.size(); i++) {
            result += ", " + domainValues.get(i).toSpeechText(inLongForm);
        }

        return result;
    }

    @Override
    public String toString() {
        return attribute + " : " + domainValues;
    }

}
