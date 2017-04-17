package planner.elements;

import planner.Speakable;

/**
 */
public class NumericalValueDomain implements Speakable {
    String attribute;
    Value lowerBound;
    Value upperBound;

    public NumericalValueDomain(String attribute, Value lowerBound, Value upperBound) {
        this.attribute = attribute;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public String toSpeechText() {
        if (lowerBound.equals(upperBound)) {
            return lowerBound.toSpeechText() + " " + attribute + " ";
        } else {
            return "between " + lowerBound.toSpeechText() + " and " + upperBound.toSpeechText() + " " + attribute;
        }
    }
}
