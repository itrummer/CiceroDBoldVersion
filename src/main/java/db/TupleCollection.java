package db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class representation of a collection of rows returned from a SQL query
 */
public class TupleCollection {

    ArrayList<Tuple> rows;

    public TupleCollection(ArrayList<Tuple> rows) {
        this.rows = rows;
    }

    public ArrayList<Tuple> getRows() {
        return rows;
    }

    public int getAttributeCount() {
        if (rows.size() == 0) {
            return 0;
        }
        return rows.get(0).getValues().size();
    }

    /**
     * Utility method to extract all tuples from a ResultSet into Rows, which are
     * then added to a TupleCollection
     * @param resultSet The ResultSet from which to read tuples
     * @return A TupleCollection representing the tuples in resultSet. Null if resultSet is null or if a
     *          SQLException is encountered
     */
    public static TupleCollection rowCollectionFromResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }

        ArrayList<Tuple> rows = new ArrayList<Tuple>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                ArrayList<Object> values = new ArrayList<Object>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String column = metaData.getColumnName(i);
                    System.out.println("Column: " + column);
                    values.add(value);
                }
                rows.add(new Tuple(values));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return new TupleCollection(rows);
    }

    @Override
    public String toString() {
        if (rows.size() == 0) {
            return "empty";
        }

        String result = "";
        for (Tuple row : rows) {
            result += row.toString() + "\n";
        }

        return result;
    }
}
