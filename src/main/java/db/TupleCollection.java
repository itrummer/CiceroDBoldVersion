package db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Class representation of a collection of Tuples returned from a SQL query
 */
public class TupleCollection {
    ArrayList<Tuple> tuples;
    ArrayList<String> attributes;

    /**
     * Constructs a TupleCollection with 0 rows.
     */
    public TupleCollection(ArrayList<String> attributes) {
        this.tuples = new ArrayList<Tuple>();
        this.attributes = attributes;
    }

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }

    public int tupleCount() {
        return tuples.size();
    }

    public String attributeForIndex(int i) {
        return attributes.get(i);
    }

    public int attributeCount() {
        return attributes.size();
    }

    public void addTuple(Tuple tuple) {
        tuples.add(tuple);
    }

    /**
     * Utility method to extract all Tuples from a ResultSet into Rows, which are
     * then added to a TupleCollection
     * @param resultSet The ResultSet from which to read Tuples
     * @return A TupleCollection representing the Tuples in resultSet. Null if resultSet is null or if a
     *          SQLException is encountered
     */
    public static TupleCollection rowCollectionFromResultSet(ResultSet resultSet) {
        if (resultSet == null) {
            return null;
        }

        try {
            ResultSetMetaData metaData = resultSet.getMetaData();

            ArrayList<String> attributes = new ArrayList<String>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                attributes.add(metaData.getColumnName(i));
            }

            TupleCollection tupleCollection = new TupleCollection(attributes);

            while (resultSet.next()) {
                Tuple tuple = new Tuple();
                ArrayList<Object> values = new ArrayList<Object>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    tuple.addValueAssignment(metaData.getColumnName(i), resultSet.getObject(i));
                }
                tupleCollection.addTuple(tuple);
            }
            return  tupleCollection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        if (tuples.isEmpty()) {
            return "empty";
        }

        String result = "";
        for (Tuple tuple : tuples) {
            result += tuple.toString() + ".\n";
        }

        return result;
    }

    public Object[] valuesForAttribute(String attribute) {
        HashSet<Object> values = new HashSet<Object>();
        for (Tuple tuple : tuples) {
            values.add(tuple.valueForAttribute(attribute));
        }
        return values.toArray();
    }

    public Object[][] getValueMatrix(boolean isCategorical) {
        if (getTuples().isEmpty()) {
            return new Object[attributeCount()][];
        }
        ArrayList<Object[]> categoricalValues = new ArrayList<Object[]>();
        ArrayList<Object[]> numericalValues = new ArrayList<Object[]>();
        for (String attribute : attributes) {
            if (getTuples().get(0).valueForAttribute(attribute).getClass().equals(String.class)) {
                categoricalValues.add(valuesForAttribute(attribute));
            } else {
                numericalValues.add(valuesForAttribute(attribute));
            }
        }
        if (isCategorical) {
            return (Object[][]) categoricalValues.toArray();
        } else {
            return  (Object[][]) numericalValues.toArray();
        }
    }

}
