package db;

import values.Value;

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
    HashMap<Integer, String> indicesForAttributes;
    HashMap<String, HashSet<Value>> valueMatrix;

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
        this.tuples = new ArrayList<Tuple>();
        this.attributes = attributes;
        this.valueMatrix = new HashMap<String, HashSet<Value>>();
        for (String a : attributes) {
            valueMatrix.put(a, new HashSet<Value>());
        }
        this.indicesForAttributes = new HashMap<Integer, String>();
        int index = 0;
        for (String a : attributes) {
            indicesForAttributes.put(index, a);
            index++;
        }

        // initialize matrices
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

    public int indexForAttribute(String a) {
        return attributes.indexOf(a);
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
            } else {
                System.out.println("FOUND DUPLICATE VALUE, SKIPPING");
            }

            // add the index from distinctValues to the indexMap matrix
            // so we can reverse lookup later to find where each tuple's
            // value for each attribute falls in the distinct value matrix
//            int i = distinctValues.indexOf(tValue);
//            System.out.println("Distinct Value: " + i);
//            indexMap.get(a).add(i);
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

    public int distinctValueCountForAttribute(int a) {
        return distinctValues.get(a).size();
    }

    public ArrayList<Value> distinctValuesForAttribute(int a) {
        return distinctValues.get(a);
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

    public int costForAttribute(int a) {
        return attributes.get(a).length();
    }

    // 2D matrix: attributeCount x distinctValueCount
    public ArrayList<ArrayList<Value>> getDistinctValueMatrix() {
        return distinctValues;
    }

    /**
     * Returns the TupleCollection as a 2D ArrayList. matrix.get(a).get(t) returns
     * Tuple t's value for attribute a
     * @return
     */
    public ArrayList<ArrayList<Value>> getValueMatrix() {
        return values;
    }


    public ArrayList<HashMap<Value, Integer>> listOfIndicesForValues() {
        // need to be able to look up for a given attribute, which of the "distinct" value indices this maps to
        ArrayList<HashMap<Value, Integer>> list = new ArrayList<HashMap<Value, Integer>>();
        ArrayList<ArrayList<Value>> attributeValueLists = getAttributeValueLists();
        for (int a = 0; a < attributeCount(); a++) {
            ArrayList<Value> distinctValuesForAttribute = attributeValueLists.get(a);
            HashMap<Value, Integer> mapOfIndices = new HashMap<Value, Integer>();
            int vIndex = 0;
            for (Value v : distinctValuesForAttribute) {
                mapOfIndices.put(v, vIndex);
                vIndex++;
            }
            list.add(mapOfIndices);
        }
        return list;
    }


}
