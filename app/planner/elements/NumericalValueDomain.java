package planner.elements;

import planner.Speakable;

/**
 * A representation of a domain of numerical Values. The lower and upper bounds are inclusive bounds.
 */
public class NumericalValueDomain extends ValueDomain implements Speakable {
    String attribute;
    Value lowerBound;
    Value upperBound;

    public NumericalValueDomain(String attribute, Value lowerBound, Value upperBound) {
        this.attribute = attribute;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public NumericalValueDomain(String attribute, Value singularValue) {
        this(attribute, singularValue, singularValue);
    }

    /**
     * Determines if a Value is within this NumericalValueDomain. This is equivalent to whether
     * a Value is both greater than or equal to the lower bound and less than or equal to the
     * upper bound of this NumericalValueDomain.
     * @param v The Value to test
     * @return True if this value is within this NumericalValueDomain. Else false.
     */
    @Override
    public boolean contains(Value v) {
        if (v == null) { return false; }
        boolean geqLowerBound = v.compareTo(lowerBound) >= 0;
        boolean leqUpperBound = v.compareTo(upperBound) <= 0;
        return geqLowerBound && leqUpperBound;
    }

    public String toSpeechText(boolean inLongForm) {
        if (lowerBound.equals(upperBound)) {
            return lowerBound.toSpeechText(inLongForm) + " " + attribute;
        } else {
            return "between " + lowerBound.toSpeechText(inLongForm) + " and " + upperBound.toSpeechText(inLongForm) + " " + attribute;
        }
    }
}
