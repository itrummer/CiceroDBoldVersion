package planning.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import planning.Speakable;
import util.EnglishNumberToWords;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a singular value. Provides various constructors for supported data types. This
 * class essentially wraps various Java types and defines useful methods for translating
 * data to speech. Values are immutable.
 */
public class Value implements Speakable, Comparable<Value> {
    /**
     * Enum that represents the supported wrapped value types. For each type,
     * there is a constructor with the corresponding Java class.
     */
    public enum ValueType {
        INTEGER,
        DOUBLE,
        FLOAT,
        STRING
    }

    private Comparable value;
    private ValueType type;
    private String cachedLongFormResult;

    /**
     * Constructs a Value that wraps an Integer
     * @param i The Integer that this Value will hold
     */
    public Value(Integer i) {
        this.value = i;
        this.type = ValueType.INTEGER;
    }

    /**
     * Constructs a Value that wraps a Double
     * @param d The Double that this Value will hold
     */
    public Value(Double d) {
        this.value = d;
        this.type= ValueType.DOUBLE;
    }

    /**
     * Constructs a Value that wraps a Float
     * @param f The Float that this Value will hold
     */
    public Value(Float f) {
        this.value = f;
        this.type = ValueType.FLOAT;
    }

    /**
     * Constructs a Value
     * @param s The String that this Value will hold
     */
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

    @Override
    public String toSpeechText(boolean inLongForm) {
        if (inLongForm) {
            if (cachedLongFormResult != null) {
                return cachedLongFormResult;
            }
            switch (type) {
                case INTEGER:
                    cachedLongFormResult = EnglishNumberToWords.convert(((Integer) value).longValue());
                    break;
                case DOUBLE:
                    Double v = (Double) value;
                    long tenths = (long) ((v - v.intValue()) * 10);
                    cachedLongFormResult = EnglishNumberToWords.convert(((Double) value).longValue()) + (tenths != 0 ? " point " + EnglishNumberToWords.convert(tenths) : "");
                    break;
                case FLOAT:
                    Float f = (Float) value;
                    long fTenths = (long) ((f - f.intValue()) * 10);
                    cachedLongFormResult = EnglishNumberToWords.convert(((Float) value).longValue()) + (fTenths != 0 ? " point " + EnglishNumberToWords.convert(fTenths) : "");
                    break;
                case STRING:
                    cachedLongFormResult = (String) value;
                    break;
            }
            cachedLongFormResult = cachedLongFormResult.trim();
            return cachedLongFormResult;
        } else {
            return value.toString();
        }
    }

    /**
     * Expands numeric types to their word values, then returns the length of the long-form number. For
     * categorical types, it returns the length of the String value.
     * @return The speech cost in characters for this Value
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

        int sigfigs = Math.max((int) Math.log10(rawValue), 0);
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
     */
    @JsonIgnore
    public boolean isCategorical() {
        return type == ValueType.STRING;
    }

    /**
     * Convenience method. Returns the negation of isCategorical()
     */
    @JsonIgnore
    public boolean isNumerical() {
        return !isCategorical();
    }

    /**
     * For numerical Values, returns a new Value whose inner value the product of this Value's inner value and
     * the multiplier is multiplied by a multiplier. For categorical Values, returns a new copy of this Value.
     * @param multiplier A double value to multiply by
     */
    public Value times(double multiplier) {
        switch (type) {
            case FLOAT:
                return new Value((float) multiplier * (Float) value);
            case DOUBLE:
                return new Value(multiplier * (Double) value);
            case INTEGER:
                return new Value((int) (multiplier * (Integer) value));
            case STRING:
                return new Value((String) value);
        }
        return null;
    }

    /**
     * Returns the inner Comparable value of this Value instance
     */
    public Comparable getValue() {
        return value;
    }
}
