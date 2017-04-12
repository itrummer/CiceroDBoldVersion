package values;

import planner.Speakable;

/**
 *
 */
public abstract class Value implements Speakable {
    public abstract String toSpeechText();

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
            return new StringValue((String) value);
        } else if (value instanceof Integer) {
            return new IntegerValue((Integer) value);
        } else if (value instanceof Double) {
            return new DoubleValue((Double) value);
        }
        // TODO: implement more types
        return null;
    }


}
