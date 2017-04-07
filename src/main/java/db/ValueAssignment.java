package db;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLType;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;

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
}
