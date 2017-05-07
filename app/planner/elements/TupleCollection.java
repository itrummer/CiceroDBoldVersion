package planner.elements;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Class representation of a collection of Tuples returned from a SQL query
 */
public class TupleCollection {
    ArrayList<String> attributes;
    ArrayList<Tuple> tuples;

    // attributeCount x tupleCount matrix
    HashMap<Integer, HashMap<Integer, Value>> values;

    // distinctValues contains only the distinct values for each attribute
    // each 'column' in this matrix is of variable length, but must be <= tupleCount
    // Example:
    //  a1    a2    a3   ...
    // "v1"   5     3    ...
    // "v2"         4    ...
    ArrayList<ArrayList<Value>> distinctValues;

    HashMap<Integer, HashSet<Value>> valueSets;

    // attributeCount x tupleCount
    // let index = indexMap.get(a).get(t)
    // distinctValues.get(a).get(t) == t's value for a
    ArrayList<ArrayList<Integer>> indexMap;

    /**
     * Constructs a TupleCollection with 0 rows.
     */
    public TupleCollection(ArrayList<String> attributes) {
        // TODO: eliminate distinctValues and just use valueSets, using the size to determine the index

        this.attributes = attributes;
        this.tuples = new ArrayList<Tuple>();
        this.values = new HashMap<>();
        this.distinctValues = new ArrayList<ArrayList<Value>>();
        this.valueSets = new HashMap<>();
        this.indexMap = new ArrayList<ArrayList<Integer>>();
        for (int a = 0; a < attributeCount(); a++) {
            values.put(a, new HashMap<>());
            distinctValues.add(new ArrayList<Value>());
            valueSets.put(a, new HashSet<>());
            indexMap.add(new ArrayList<Integer>());
        }
    }

    public ArrayList<Tuple> getTuples() {
        return tuples;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
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
        int tupleIndex = tuples.size()-1;

        for (int a = 0; a < attributeCount(); a++) {
            String attributeName = attributes.get(a);
            Value tValue = tuple.valueForAttribute(attributeName);

            // add the value to the matrix
            HashMap<Integer, Value> aValues = values.get(a);
            aValues.put(tupleIndex, tValue);

            // add any new values to the distinctValues matrix
            ArrayList<Value> aDistinctValues = distinctValues.get(a);
            if (!valueSets.get(a).contains(tValue)) {
                aDistinctValues.add(tValue);
                valueSets.get(a).add(tValue);
            }

            if (tValue.isNumerical()) {
                for (Value v : tValue.roundedValues()) {
                    if (!valueSets.get(a).contains(v)) {
                        aDistinctValues.add(v);
                        valueSets.get(a).add(v);
                    }
                }
            }
        }
    }

    /**
     * Returns true if the values for the attribute at index a are Categorical Values
     * @param a The index of an attribute
     * @return True is Values for attribute a are numerical
     */
    public boolean attributeIsCategorical(int a) {
        return values.get(a).get(0).isCategorical();
    }

    /**
     * Returns true if the values for the attribute at index a are Numerical Values
     * @param a The index of an attribute
     * @return True is Values for attribute a are numerical
     */
    public boolean attributeIsNumerical(int a) {
        return !attributeIsCategorical(a);
    }

    /**
     * Calculates which index in the distinct value matrix corresponds to Tuple t's Value for
     * attribute a. This is used in the Linear Programming model, since we must create variables
     * that correspond to distinct Values and match them to Contexts. This method will then
     * help determine which distinct value, and thus which Contexts, this Tuple is mapped to.
     * @param a The attribute index
     * @param t The Tuple index
     * @return The index of the distinct Value for attribute a
     */
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

    /**
     * Returns an int array containing the number of distinct Categorical values for each attribute. If
     * a given attribute a is not Categorical, then the value for entry int[a] is 0.
     * @return An int array of size attributeCount()
     */
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

    /**
     * Returns an int array containing the number of distinct Numerical values for each attribute. If
     * a given attribute a is not Numerical, then the value for entry int[a] is 0.
     * @return An int array of size attributeCount()
     */
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

    /**
     * Returns the v'th distinct Value for attribute a
     * @param a The index of the attribute
     * @param v The index of the distinct Value
     * @return A Value object
     */
    public Value getDistinctValue(int a, int v) {
        return distinctValues.get(a).get(v);
    }

    /**
     * Returns the number of distinct Values in attribute a for this TupleCollection
     * @param a An attribute index
     * @return The distinct Value count for attribute a
     */
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
                Tuple tuple = new Tuple(attributes);
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
     * Calculates the cost of an attribute. This is the number of characters in the attribute name.
     * @param a The index of the attribute
     * @return The cost of the attribute at index a
     */
    public int costForAttribute(int a) {
        return attributes.get(a).length();
    }

    /**
     * Computes the set of candidate domain assignments for all attributes. Used in
     * the GreedyPlanner and HybridPlanner algorithms.
     */
    public HashMap<Integer, HashSet<ValueDomain>> candidateAssignments(int mC, double mW) {
        HashMap<Integer, HashSet<ValueDomain>> attributeDomains = new HashMap<>();
        for (int a = 0; a < attributeCount(); a++) {
            if (a == getPrimaryKeyIndex()) {
                continue;
            }
            HashSet<ValueDomain> domains = new HashSet<>();
            if (attributeIsCategorical(a)) {
                distinctValues.get(a).toArray();
                HashSet<Value> valueSet = new HashSet<>();
                valueSet.addAll(distinctValues.get(a));

                // add subsets of bounded cardinality
                HashSet<HashSet<Value>> valueSubsets = subsetsOfSize(valueSet, mC);
                for (HashSet<Value> subset : valueSubsets) {
                    domains.add(new CategoricalValueDomain(attributeForIndex(a), new ArrayList<>(subset)));
                }
            } else if (attributeIsNumerical(a)) {
                // add intervals of bounded width
                for (int b1 = 0; b1 < distinctValueCountForAttribute(a); b1++) {
                    Value v1 = getDistinctValue(a, b1);

                    for (int b2 = b1; b2 < distinctValueCountForAttribute(a); b2++) {
                        Value v2 = getDistinctValue(a, b2);
                        NumericalValueDomain candidateDomain = new NumericalValueDomain(attributeForIndex(a), v1, v2);
                        if (candidateDomain.getWidth() <= mW) {
                            domains.add(candidateDomain);
                        }
                    }
                }
            }
            attributeDomains.put(a, domains);
        }

        return attributeDomains;
    }

    /**
     * For now, we assume that the first attribute is the primary key of this tuple collection
     */
    public int getPrimaryKeyIndex() {
        return 0;
    }

    /**
     * Computes the power set of a set of items
     * @param originalSet The original set of items
     * @param <T> The type of the items
     * @return The power set of the original set
     */
    public static <T> HashSet<HashSet<T>> powerSet(HashSet<T> originalSet) {
        HashSet<HashSet<T>> sets = new HashSet<HashSet<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        HashSet<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (HashSet<T> set : powerSet(rest)) {
            HashSet<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    /**
     * Computes all subsets of originalSet that have at most k elements. Excludes the empty set for convenience.
     * @param originalSet The set of objects from which to create subsets
     * @param k The maximum allowed size of a subset
     * @param <T> The type of the objects in the HashSet
     * @return All subsets of the original set that contain at most k elements, excluding the empty set
     */
    public static <T> HashSet<HashSet<T>> subsetsOfSize(HashSet<T> originalSet, int k) {
        HashSet<HashSet<T>> filteredSets = new HashSet<>();
        for (HashSet<T> set : powerSet(originalSet)) {
            if (set.size() <= k && set.size() > 0) {
                filteredSets.add(set);
            }
        }
        return filteredSets;
    }

    @Override
    public String toString() {
        if (tupleCount() == 0) {
            return "TupleCollection: empty";
        }

        String result = "";
        for (Tuple tuple : tuples) {
            result += tuple.toString() + ", ";
        }
        return result.substring(result.length()-2);
    }

    public static void main(String[] args) {
        HashSet<Integer> testSet = new HashSet<>();
        testSet.add(1);
        testSet.add(2);
        testSet.add(3);
        testSet.add(4);
        System.out.println(TupleCollection.powerSet(testSet));
        System.out.println(TupleCollection.subsetsOfSize(testSet, 2));
    }
}
