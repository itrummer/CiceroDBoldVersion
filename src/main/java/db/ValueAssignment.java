package db;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Assigns a column name to a value for a specific Tuple
 */
public class ValueAssignment {
    public static HashMap<String, Class> TYPES;
    static {
        TYPES = new HashMap<String, Class>();
        TYPES.put("INTEGER", Integer.class);
        TYPES.put("TINYINT", Byte.class);
        TYPES.put("SMALLINT", Short.class);
        TYPES.put("BIGINT", Long.class);
        TYPES.put("REAL", Float.class);
        TYPES.put("FLOAT", Double.class);
        TYPES.put("DOUBLE", Double.class);
        TYPES.put("DECIMAL", BigDecimal.class);
        TYPES.put("NUMERIC", BigDecimal.class);
        TYPES.put("BOOLEAN", Boolean.class);
        TYPES.put("CHAR", String.class);
        TYPES.put("VARCHAR", String.class);
        TYPES.put("LONGVARCHAR", String.class);
        TYPES.put("DATE", Date.class);
        TYPES.put("TIME", Time.class);
        TYPES.put("TIMESTAMP", Timestamp.class);
    }

    String column;
    Object value;

    public ValueAssignment(String column, Object value) {
        this.column = column;
        this.value = value;
    }

}
