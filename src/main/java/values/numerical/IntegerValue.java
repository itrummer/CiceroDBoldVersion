package values.numerical;

import values.numerical.NumericalValue;

/**
 */
public class IntegerValue extends NumericalValue {
    Integer value;

    public IntegerValue(Integer value) {
        this.value = value;
    }

    @Override
    double getLinearProgrammingCoefficient() {
        return (double) value;
    }

    @Override
    public String toSpeechText() {
        return toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
