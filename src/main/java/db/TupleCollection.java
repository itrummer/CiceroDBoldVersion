package db;

import values.Value;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Class representation of a collection of Tuples returned from a SQL query
 */
public class TupleCollection {
    ArrayList<String> attributes;
    ArrayList<Tuple> tuples;

    // attributeCount x tupleCount matrix
    ArrayList<ArrayList<Value>> values;

    // distinctValues contains only the distinct values for each attribute
    // each 'column' in this matrix is of variable length, but must be <= tupleCount
    // Example:
    //  a1    a2    a3   ...
    // "v1"   5     3    ...
    // "v2"         4    ...
    ArrayList<ArrayList<Value>> distinctValues;

    // attributeCount x tupleCount
    // let index = indexMap.get(a).get(t)
    // distinctValues.get(a).get(t) == t's value for a
    ArrayList<ArrayList<Integer>> indexMap;

    /**
     * Constructs a TupleCollection with 0 rows.
     */
    public TupleCollection(ArrayList<String> attributes) {
        this.attributes = attributes;
        this.tuples = new ArrayList<Tuple>();
        this.values = new ArrayList<ArrayList<Value>>();
        this.distinctValues = new ArrayList<ArrayList<Value>>();
        this.indexMap = new ArrayList<ArrayList<Integer>>();
        for (int a = 0; a < attributeCount(); a++) {
            values.add(new ArrayList<Value>());
            distinctValues.add(new ArrayList<Value>());
            indexMap.add(new ArrayList<Integer>());
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

    public int attributeCount() {
        return attributes.size();
    }

    public void addTuple(Tuple tuple) {
        tuples.add(tuple);

        for (int a = 0; a < attributeCount(); a++) {
            String attributeName = attributes.get(a);
            Value tValue = tuple.valueForAttribute(attributeName);

            // add the value to the matrix
            ArrayList<Value> aValues = values.get(a);
            aValues.add(tValue);

            // add any new values to the distinctValues matrix
            ArrayList<Value> aDistinctValues = distinctValues.get(a);
            if (!distinctValues.contains(tValue)) {
                aDistinctValues.add(tValue);
            }
        }
    }

    public boolean attributeIsCategorical(int a) {
        return values.get(a).get(0).isCategorical();
    }

    public boolean attributeIsNumerical(int a) {
        return !attributeIsCategorical(a);
    }

    public int getIndexOfDistinctValue(int a, int t) {
        Value v = getValueForAttributeAndTuple(a, t);
        ArrayList<Value> values = distinctValues.get(a);
        for (int i = 0; i < values.size(); i++) {
            if (v.equals(values.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public int[] getLengthsOfCategoricalAttributes() {
        int[] lengths = new int[attributeCount()];
        for (int a = 0; a < attributeCount(); a++) {
            if (attributeIsCategorical(a)) {
                lengths[a] = distinctValueCountForAttribute(a);
            } else {
                lengths[a] = 0;
            }
        }
        return lengths;
    }

    public int[] getLengthsOfNumericalAttributes() {
        int[] lengths = new int[attributeCount()];
        for (int a = 0; a < attributeCount(); a++) {
            if (attributeIsNumerical(a)) {
                lengths[a] = distinctValueCountForAttribute(a);
            } else {
                lengths[a] = 0;
            }
        }
        return lengths;
    }

    /**
     * Returns the Value of Tuple t that corresponds to attribute at index a
     * @param a The index of the desired attribute
     * @param t The index of the desired tuple
     * @return A Value object
     */
    public Value getValueForAttributeAndTuple(int a, int t) {
        return tuples.get(t).valueForAttribute(attributes.get(a));
    }

    public Value getDistinctValue(int a, int v) {
        return distinctValues.get(a).get(v);
    }

    public int distinctValueCountForAttribute(int a) {
        return distinctValues.get(a).size();
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

    public int costForAttribute(int a) {
        return attributes.get(a).length();
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
