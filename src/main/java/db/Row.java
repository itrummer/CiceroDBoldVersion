package db;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Represents a row in a database table
 */
public class Row {
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

    public HashMap<String, Object> attributeValuePairs;

    public Row() {
        this(new HashMap<String, Object>());
    }

    public Row(HashMap<String, Object> attributeValuePairs) {
        this.attributeValuePairs = attributeValuePairs;
    }

    public Set<String> getAttributes() {
        return attributeValuePairs.keySet();
    }

    public Collection<Object> getValues() {
        return attributeValuePairs.values();
    }

    @Override
    public String toString() {
        String result = "(";
        for (Object value : getValues()) {
            result += value.toString() + "\t";
        }
        result += ")";
        return result;
    }
}

/**
 *
 *
 *
 * class Row
 {
 public Map <Object,Class> row;
 public static Map <String, Class> TYPE;


 public void add<t> (t data)
 {
 row.put(data, data.getClass());
 }

 public void add (Object data, String sqlType)
 {
 add((Row.TYPE.get(sqlType)) data);
 }

 public static void formTable (ResultSet rs, ArrayList<Row> table)
 {
 if (rs == null) return;

 ResultSetMetaData rsmd = rs.getMetaData();

 int NumOfCol = rsmd.getColumnCount();

 while (rs.next())
 {
 row = new Row ();

 for(int i = 1; i <= NumOfCol; i++)
 {
 row.add(rs.getObject(i), rsmd.getColumnTypeName(i));
 }

 table.add(row);
 }
 }
 }
 */
