package db;

/**
 * Assigns a column name to a value for a specific Tuple
 */
public class ValueAssignment {
    String column;
    Object value;

    public ValueAssignment(String column, Object value) {
        this.column = column;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return column + " : " + value;
    }

    public String toSpeechText() {
        // TODO: translate numbers to words for better calculation of time costs
        return toString();
    }

    public boolean isCategorical() {
        return value instanceof String;
    }
}
