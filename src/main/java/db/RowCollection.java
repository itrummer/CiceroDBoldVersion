package db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mabryan on 3/25/17.
 */
public class RowCollection {

    ArrayList<Row> rows;

    public RowCollection(ArrayList<Row> rows) {
        this.rows = rows;
    }

    public static RowCollection rowCollectionFromResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }

        ArrayList<Row> rows = new ArrayList<Row>();

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                HashMap<String, Object> attributeValuePairs = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    String column = metaData.getColumnClassName(i);
                    Object value = resultSet.getObject(i);
                    attributeValuePairs.put(column, value);
                }
                rows.add(new Row(attributeValuePairs));
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
