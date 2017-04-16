package values;

import planner.Speakable;

import java.util.ArrayList;

/**
 */
public class CategoricalValueDomain implements Speakable {
    String attribute;
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

    public String toSpeechText() {
        if (domainValues.size() == 1) {
            // example: "category Italian"
            return attribute + " " + domainValues.get(0).toSpeechText();
        }

        // example: "category Italian or American"
        if (domainValues.size() == 2) {
            return attribute + " " + domainValues.get(0).toSpeechText() + " or " + domainValues.get(1).toSpeechText();
        }

        // example: "category Italian, American, or Pub Food"
        String result = attribute + " " + domainValues.get(0).toSpeechText();
        for (int i = 1; i < domainValues.size(); i++) {
            result += ", " + domainValues.get(i).toSpeechText();
        }

        return result;
    }
}
