package db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class representation of a collection of rows returned from a SQL query
 */
public class RowCollection {

    ArrayList<Row> rows;

    public RowCollection(ArrayList<Row> rows) {
        this.rows = rows;
    }

    /**
     * Utility method to extract all tuples from a ResultSet into Rows, which are
     * then added to a RowCollection
     * @param resultSet The ResultSet from which to read tuples
     * @return A RowCollection representing the tuples in resultSet. Null if resultSet is null or if a
     *          SQLException is encountered
     */
    public static RowCollection rowCollectionFromResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }

        ArrayList<Row> rows = new ArrayList<Row>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                ArrayList<Object> values = new ArrayList<Object>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    values.add(value);
                }
                rows.add(new Row(values));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return new RowCollection(rows);
    }

    @Override
    public String toString() {
        if (rows.size() == 0) {
            return "empty";
        }

        String result = "";
        for (Row row : rows) {
            result += row.toString() + "\n";
        }

        return result;
    }
}
