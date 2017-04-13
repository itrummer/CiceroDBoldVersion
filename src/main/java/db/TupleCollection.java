package db;

import values.CategoricalValue;
import values.NumericalValue;
import values.Value;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Class representation of a collection of Tuples returned from a SQL query
 */
public class TupleCollection {
    ArrayList<Tuple> tuples;
    ArrayList<String> attributes;
    HashMap<String, HashSet<Value>> valueMatrix;

    /**
     * Constructs a TupleCollection with 0 rows.
     */
    public TupleCollection(ArrayList<String> attributes) {
        this.tuples = new ArrayList<Tuple>();
        this.attributes = attributes;
        this.valueMatrix = new HashMap<String, HashSet<Value>>();
        for (String a : attributes) {
            valueMatrix.put(a, new HashSet<Value>());
        }
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

    public int indexForAttribute(String a) {
        return attributes.indexOf(a);
    }

    public int attributeCount() {
        return attributes.size();
    }

    public void addTuple(Tuple tuple) {
        // add new values as we add new tuples
        for (String a : attributes) {
            valueMatrix.get(a).add(tuple.valueForAttribute(a));
        }
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
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    tuple.addValueAssignment(metaData.getColumnName(i), Value.createValueObject(resultSet.getObject(i)));
                }
                tupleCollection.addTuple(tuple);
            }
            return  tupleCollection;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Both Categorical and Numerical values
     * @return
     */
    public ArrayList<ArrayList<Value>>  getAttributeValueLists() {
        if (tupleCount() == 0 || attributeCount() == 0) {
            return new ArrayList<ArrayList<Value>>();
        }
        // here we are guaranteed to have at least one value for each list and at least one attribute
        // this is so we can call isCategorical() on the first attribute
        ArrayList<ArrayList<Value>> attributesAndValues = new ArrayList<ArrayList<Value>>();
        for (int a = 0; a < attributes.size(); a++) {
            ArrayList<Value> valueList = new ArrayList<Value>();
            valueList.addAll(valueMatrix.get(attributes.get(a)));
            attributesAndValues.add(valueList);
        }
        return attributesAndValues;
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
}
