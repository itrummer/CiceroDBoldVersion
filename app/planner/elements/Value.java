package planner.elements;

import planner.Speakable;
import util.EnglishNumberToWords;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Value implements Speakable, Comparable<Value> {
    public enum ValueType {
        INTEGER,
        DOUBLE,
        FLOAT,
        STRING
    }

    Comparable value;
    ValueType type;

    public Value(Integer i) {
        this.value = i;
        this.type = ValueType.INTEGER;
    }

    public Value(Double d) {
        this.value = d;
        this.type= ValueType.DOUBLE;
    }

    public Value(Float f) {
        this.value = f;
        this.type = ValueType.FLOAT;
    }

    public Value(String s) {
        this.value = s;
        this.type = ValueType.STRING;
    }

    /**
     * Compares this to Value v. In the current implementation, if the types are not equal we will say that the
     * two values are not equal. Thus, we will determine compareTo by the order of ValueTypes, i.e.
     * INTEGERs will be less than DOUBLEs, DOUBLEs less than STRINGs, and so on. This will be useful if
     * we need to sort a collection of mixed types, i.e. types will be grouped together.
     * @param anotherValue The Value to compare to
     * @return A negative int if this is less than v, 0 if the Values are equal, and a positive int if
     * this is greater than v.
     */
    public int compareTo(Value anotherValue) {
        if (type != anotherValue.type) {
            Integer thisValue = type.ordinal();
            Integer thatValue = anotherValue.type.ordinal();
            return thisValue.compareTo(thatValue);
        }
        return value.compareTo(anotherValue.value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Value && compareTo((Value) obj) == 0;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Helper function for building the correct CategoricalValue or NumericalValue type. This is
     * useful because TupleCollection can cast once from the SQL data type and can deal with
     * the specific value. Also, we can control which data types we support in this method.
     *
     * @param value The value to construct
     * @return A Value object of the correct type. Null if value does not conform to a supported type.
     */
    public static Value createValueObject(Object value) {
        // TODO: may not handle nulls from SQL correctly.. will need to test this out
        if (value instanceof String) {
            return new Value((String) value);
        } else if (value instanceof Integer) {
            return new Value((Integer) value);
        } else if (value instanceof Double) {
            return new Value((Double) value);
        } else if (value instanceof Float) {
            return new Value((Float) value);
        }
        return null;
    }

    public String toSpeechText(boolean inLongForm) {
        if (inLongForm) {
            String expandedValue = "";
            switch (type) {
                case INTEGER:
                    expandedValue = EnglishNumberToWords.convert(((Integer) value).longValue());
                    break;
                case DOUBLE:
                    Double v = (Double) value;
                    long tenths = (long) ((v - v.intValue()) * 10);
                    expandedValue = EnglishNumberToWords.convert(((Double) value).longValue()) + " point " + EnglishNumberToWords.convert(tenths);
                    break;
                case FLOAT:
                    expandedValue = EnglishNumberToWords.convert(((Float) value).longValue());
                    break;
                case STRING:
                    expandedValue = (String) value;
                    break;
            }
            return expandedValue;
        } else {
            return value.toString();
        }
    }

    /**
     * Expands numeric types to their word values, then returns the length of the long-form number. For
     * Categorical types, it returns the length of the String value.
     * @return The speech cost for this Value
     */
    public int speechCost() {
        return toSpeechText(true).length();
    }

    public String toString() {
        return value.toString();
    }

    public double linearProgrammingCoefficient() {
        double coefficient = 0.0;
        switch (type) {
            case DOUBLE:
                coefficient = ((Double) value);
                break;
            case INTEGER:
                coefficient = ((Integer) value).doubleValue();
                break;
            case FLOAT:
                coefficient = ((Float) value).doubleValue();
        }
        return coefficient;
    }

    public List<Value> roundedValues() {
        double coefficient = linearProgrammingCoefficient();
        boolean negative = coefficient < 0;
        int rawValue = (int) coefficient;

        if (negative) {
            rawValue = -rawValue;
        }

        int sigfigs = (int) Math.log10(rawValue);
        int mostSignificantUnits = (int) Math.pow(10, sigfigs);
        int lower = rawValue / mostSignificantUnits;
        int upper = lower + 1;
        Integer roundedDown = mostSignificantUnits * lower;
        Integer roundedUp = mostSignificantUnits * upper;

        // negate after calculating values
        if (negative) {
            roundedDown = -roundedDown;
            roundedUp = -roundedUp;
        }

        List<Value> values = new ArrayList<>();
        values.add(roundingHelper(roundedDown.doubleValue()));
        values.add(roundingHelper(roundedUp.doubleValue()));

        return values;
    }

    private Value roundingHelper(Double v) {
        switch (type) {
            case FLOAT:
                return new Value(v.floatValue());
            case DOUBLE:
                return new Value(v);
            case INTEGER:
                return new Value(v.intValue());
        }
        return null;
    }

    /**
     * Determines if this Value is classified as categorical. Categorical values are Strings. Any valid Value
     * instance that is not Categorical is classified as Numerical.
     * @return
     */
    public boolean isCategorical() {
        return type == ValueType.STRING;
    }

    /**
     * Convenience method. Returns the negation of isCategorical()
     */
    public boolean isNumerical() {
        return !isCategorical();
    }

}
