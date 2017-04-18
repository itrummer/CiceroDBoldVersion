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

    public String toSpeechText(boolean inLongForm) {
        if (lowerBound.equals(upperBound)) {
            return lowerBound.toSpeechText(inLongForm) + " " + attribute;
        } else {
            return "between " + lowerBound.toSpeechText(inLongForm) + " and " + upperBound.toSpeechText(inLongForm) + " " + attribute;
        }
    }
}
