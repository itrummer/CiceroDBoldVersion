package db;

import values.CategoricalValue;
import values.NumericalValue;
import values.Value;

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

    /**
     * Constructs the matrix of unique categorical values that appear as values in Tuples in this TupleCollection.
     * @return The distinct categorical values that appear in this TupleCollection. Let result : Object[][] be the matrix
     * returned by this method, result[a] contains the array of distinct categorical values for categorical attribute a
     */
    public CategoricalValue[][] getCategoricalValueMatrix() {
        // TODO: create better workaround for avoiding using the toArray() method on the ArrayList and HashSet
        // We cannot use this convenient method since casting an Object[] array to Value[] array throws a runtime exception
        ArrayList<CategoricalValue[]> categoricalValues = new ArrayList<CategoricalValue[]>();
        for (String a : attributes) {
            // use HashSet to eliminate duplicates
            HashSet<CategoricalValue> valuesForAttribute = new HashSet<CategoricalValue>();
            for (Tuple t : getTuples()) {
                Value v = t.valueForAttribute(a);
                if (v instanceof CategoricalValue) {
                    valuesForAttribute.add((CategoricalValue) v);
                }
            }
            CategoricalValue[] temp = new CategoricalValue[valuesForAttribute.size()];
            int i = 0;
            for (CategoricalValue v : valuesForAttribute) {
                temp[i] = v;
                i++;
            }
            categoricalValues.add(temp);
        }
        CategoricalValue[][] result = new CategoricalValue[categoricalValues.size()][];
        for (int i = 0; i < categoricalValues.size(); i++) {
            result[i] = categoricalValues.get(i);
        }
        return result;
    }

    /**
     * Constructs the matrix of unique numerical values that appear as values in Tuples in this TupleCollection.
     * @return The distinct numerical values that appear in this TupleCollection. Let result : Object[][] be the matrix
     * returned by this method, result[a] contains the array of distinct numerical values for numerical attribute a
     */
    public NumericalValue[][] getNumericalValueMatrix() {
        // TODO: create better workaround for avoiding using the toArray() method on the ArrayList and HashSet
        // We cannot use this convenient method since casting an Object[] array to Value[] array throws a runtime exception
        ArrayList<NumericalValue[]> numericalValues = new ArrayList<NumericalValue[]>();
        for (String a : attributes) {
            // use HashSet to eliminate duplicates
            HashSet<NumericalValue> valuesForAttribute = new HashSet<NumericalValue>();
            for (Tuple t : getTuples()) {
                Value v = t.valueForAttribute(a);
                if (v instanceof NumericalValue) {
                    valuesForAttribute.add((NumericalValue) v);
                }
            }
            NumericalValue[] temp = new NumericalValue[valuesForAttribute.size()];
            int i = 0;
            for (NumericalValue v : valuesForAttribute) {
                temp[i] = v;
                i++;
            }
            numericalValues.add(temp);
        }
        NumericalValue[][] result = new NumericalValue[numericalValues.size()][];
        for (int i = 0; i < numericalValues.size(); i++) {
            result[i] = numericalValues.get(i);
        }
        return result;
    }
}
