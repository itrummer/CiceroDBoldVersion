package planner.elements;

import planner.Speakable;

/**
 * A representation of a domain of numerical Values. The lower and upper bounds are inclusive bounds.
 */
public class NumericalValueDomain extends ValueDomain {
    public static final String BETWEEN_UPPER_AND_LOWER_PHRASE = " to ";

    Value lowerBound;
    Value upperBound;
    String longFormCachedResult;
    String shortFormCachedResult;

    public NumericalValueDomain(String attribute, Value v1, Value v2) {
        this.attribute = attribute;
        this.shortFormCachedResult = null;
        this.longFormCachedResult = null;
        if (v1.compareTo(v2) <= 0) {
            this.lowerBound = v1;
            this.upperBound = v2;
        } else {
            this.lowerBound = v2;
            this.upperBound = v1;
        }
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
        if (v == null || !v.isNumerical()) { return false; }
        boolean geqLowerBound = v.compareTo(lowerBound) >= 0;
        boolean leqUpperBound = v.compareTo(upperBound) <= 0;
        return geqLowerBound && leqUpperBound;
    }

    @Override
    public boolean isCategorical() {
        return false;
    }

    @Override
    public boolean isNumerical() {
        return true;
    }

    /**
     * Computes the width of this numerical value domain, which we define as
     * how much bigger the upper bound is than the lower bound. That is,
     * lowerBound * getWidth() = upperBound
     * @return
     */
    public double getWidth() {
        return upperBound.linearProgrammingCoefficient() / lowerBound.linearProgrammingCoefficient();
    }

    public String toSpeechText(boolean inLongForm) {
        if (inLongForm && longFormCachedResult != null) {
            return longFormCachedResult;
        } else if (!inLongForm && shortFormCachedResult != null) {
            return shortFormCachedResult;
        }

        StringBuilder result = new StringBuilder("");

        if (lowerBound.equals(upperBound)) {
            result.append(lowerBound.toSpeechText(inLongForm));
            result.append(" ");
            result.append(attribute);
        } else {
            result.append(lowerBound.toSpeechText(inLongForm));
            result.append(BETWEEN_UPPER_AND_LOWER_PHRASE);
            result.append(upperBound.toSpeechText(inLongForm));
            result.append(" ");
            result.append(attribute);
        }

        if (inLongForm) {
            longFormCachedResult = result.toString();
            return longFormCachedResult;
        } else {
            shortFormCachedResult = result.toString();
            return shortFormCachedResult;
        }
    }

    public static int speechCostForUnequalBounds() {
        return BETWEEN_UPPER_AND_LOWER_PHRASE.length();
    }

    @Override
    public String toString() {
        return attribute + ": " + lowerBound + " to " + upperBound;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NumericalValueDomain)) {
            return false;
        }

        NumericalValueDomain otherDomain = (NumericalValueDomain) obj;
        return attribute.equals(otherDomain.attribute) && lowerBound.equals(otherDomain.lowerBound) && upperBound.equals(otherDomain.upperBound);
    }

    @Override
    public int hashCode() {
        return lowerBound.hashCode() + upperBound.hashCode();
    }
}
