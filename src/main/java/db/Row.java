package db;

import planner.PlanItem;
import planner.VoicePlanner;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a row in a database table
 */
public class Row implements PlanItem {
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

    public ArrayList<Object> values;

    public Row() {
        this(new ArrayList<Object>());
    }

    public Row(ArrayList<Object> values) {
        this.values = values;
    }

    public ArrayList<Object> getValues() {
        return values;
    }

    public void accept(VoicePlanner voicePlanner) {
        voicePlanner.visit(this);
    }

    @Override
    public String toString() {
        String result = "";
        for (Object value : getValues()) {
            result += value.toString() + "\t";
        }
        return result;
    }
}
