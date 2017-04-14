package values;

import planner.Speakable;

/**
 *
 */
public class Value implements Speakable, Comparable<Value> {
    public enum ValueType {
        INTEGER,
        DOUBLE,
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
            Integer thisValue = new Integer(type.ordinal());
            Integer thatValue = new Integer(anotherValue.type.ordinal());
            return thisValue.compareTo(thatValue);
        }
        return value.compareTo(anotherValue.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            return compareTo((Value) obj) == 0;
        }
        return false;
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
        }
        return null;
    }

    public String toSpeechText() {
        // TODO: implement based on type
        switch (type) {
            case INTEGER:
                break;
            case DOUBLE:
                break;
            case STRING:
                break;
        }
        return value.toString();
    }

    public String toString() {
        return "<" + type.name() + ": " + value.toString() + ">";
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
        }
        return coefficient;
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
