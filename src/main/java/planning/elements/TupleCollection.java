package planning.elements;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Class representation of a collection of Tuples returned from a SQL query
 */
public class TupleCollection implements Iterable<Tuple> {
    List<String> attributes;
    Map<Integer, Tuple> tuples;
    Map<Integer, Map<Integer, Value>> values;
    Map<Integer, Map<Integer, Value>> distinctValues;
    HashMap<Integer, HashSet<Value>> valueSets;

    /**
     * Constructs a TupleCollection with 0 rows.
     */
    public TupleCollection(List<String> attributes) {
        this.attributes = attributes;
        this.tuples = new HashMap<>();
        this.values = new HashMap<>();
        this.distinctValues = new HashMap<>();
        this.valueSets = new HashMap<>();
        for (int a = 0; a < attributeCount(); a++) {
            values.put(a, new HashMap<>());
            distinctValues.put(a, new HashMap<>());
            valueSets.put(a, new HashSet<>());
        }
    }

    @JsonUnwrapped
    public ArrayList<Tuple> getTuples() {
        ArrayList<Tuple> list = new ArrayList<>();
        for (Tuple t : this) {
            list.add(t);
        }
        return list;
    }

    public Tuple getTuple(int t) {
        return tuples.get(t);
    }

    public List<String> getAttributes() {
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

    /**
     * Inserts a Tuple into this TupleCollection. Also adds any new distinct values that appear in
     * the Tuple to this TupleCollection's set of distinct values for each attribute.
     * @param tuple The Tuple to be added to this TupleCollection
     */
    public void addTuple(Tuple tuple) {
        int tupleIndex = tuples.size();
        tuples.put(tupleIndex, tuple);

        for (int a = 0; a < attributeCount(); a++) {
            Value tValue = tuple.valueForAttribute(attributes.get(a));
            addValueForTuple(a, tupleIndex, tValue);
            addDistinctValue(a, tValue);
            if (tValue.isNumerical()) {
                for (Value v : tValue.roundedValues()) {
                    addDistinctValue(a, v);
                }
            }
        }
    }

    /**
     * Adds a Value to the set of distinct Values that appear for attribute a in this TupleCollection. There is
     * no change if Value v already appears as a distinct Value for attribute a. We also store the index
     * at which this distinct Value is seen during the insertion of this Value.
     * @param a The index of the attribute
     * @param v The distinct Value to add to attribute a
     */
    private void addDistinctValue(int a, Value v) {
        if (!valueSets.get(a).contains(v)) {
            int index = distinctValues.get(a).size();
            distinctValues.get(a).put(index, v);
            valueSets.get(a).add(v);
        }
    }

    /**
     * Stores Value v as the value for attribute a for the Tuple inserted at index t
     */
    private void addValueForTuple(int a, int t, Value v) {
        values.get(a).put(t, v);
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
        Map<Integer, Value> values = distinctValues.get(a);
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

            ArrayList<String> attributes = new ArrayList<>();
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
    public Map<Integer, Set<ValueDomain>> candidateAssignments(int mC, double mW) {
        Map<Integer, Set<ValueDomain>> attributeDomains = new HashMap<>();
        for (int a = 0; a < attributeCount(); a++) {
            if (a == getPrimaryKeyIndex()) {
                continue;
            }
            Set<ValueDomain> domains = new HashSet<>();
            if (attributeIsCategorical(a)) {
                Set<Value> valueSet = new HashSet<>();
                valueSet.addAll(distinctValues.get(a).values());

                // add subsets of bounded cardinality
                Set<Set<Value>> valueSubsets = subsetsOfSize(valueSet, mC);
                for (Set<Value> subset : valueSubsets) {
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

    public Set<ValueDomain> candidateAssignmentSet(int mC, double mW) {
        Set<ValueDomain> result = new HashSet<>();
        for (Set<ValueDomain> set : candidateAssignments(mC, mW).values()) {
            result.addAll(set);
        }
        return result;
    }

    /**
     * For now, we assume that the first attribute is the primary key of this tuple collection
     */
    public int getPrimaryKeyIndex() {
        return 0;
    }

    public double entropy(double mW) {
        double totalEntropy = 0.0;
        for (int a = 0; a < attributeCount(); a++) {
            totalEntropy += entropyForAttribute(a, mW);
        }

        return totalEntropy / (double) attributeCount();
    }

    /**
     * Computes the entropy given an array of counts corresponding to the number of Values found in a given
     * "bucket". To compute entropy, we assume there are discrete Values or independent partitions or ranges
     * of Values, in the case of numerical values. Thus, given a count, we can compute the probability, and
     * then can calculate how many values share this probability. Each value with this same probability and
     * count will contribute the same amount of entropy to the total entropy, so we avoid having to recalculate
     * the same probability multiple times.
     */
    private double entropyForCounts(int[] counts) {
        int totalCount = 0;
        for (int i = 0; i < counts.length; i++) {
            totalCount += counts[i];
        }

        double totalEntropy = 0.0;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == 0) {
                continue;
            }

            double p = (double) counts[i] / (double) totalCount;
            double entropy = -p * Math.log(p);
            totalEntropy += entropy;
        }

        return totalEntropy;
    }

    private double entropyForAttribute(int a, double mW) {
        int[] counts = new int[0];

        if (attributeIsCategorical(a) || mW <= 1.0) {
            counts = distinctValueCounts(a);
        } else if (attributeIsNumerical(a)) {
            Value[] tupleValues = new Value[tupleCount()];
            for (int t = 0; t < tupleCount(); t++) {
                tupleValues[t] = getValueForAttributeAndTuple(a, t);
            }

            Arrays.sort(tupleValues, new Comparator<Value>() {
                @Override
                public int compare(Value o1, Value o2) {
                    return o1.compareTo(o2);
                }
            });

            List<Integer> countList = new ArrayList<>();

            Value startOfNextInterval = tupleValues[0].times(mW);
            int currentCount = 0;
            int i = 0;
            while (i < tupleValues.length) {
                Value current = tupleValues[i];
                if (current.compareTo(startOfNextInterval) < 0) {
                    // increment count and keep interval the same
                    currentCount++;
                    i++;
                } else {
                    // move interval so that we now start at startOfNextInterval and adjust the next intervals end
                    if (currentCount > 0) {
                        countList.add(currentCount);
                    }
                    startOfNextInterval = startOfNextInterval.times(mW);
                    currentCount = 0;
                }
            }

            int c = 0;
            counts = new int[countList.size()];
            for (Integer count : countList) {
                counts[c] = count;
                c++;
            }
        }

        return entropyForCounts(counts);
    }

    private int[] distinctValueCounts(int a) {
        Map<Value, Integer> valueCounts = new HashMap<>();
        for (int t = 0; t < tupleCount(); t++) {
            Value v = getValueForAttributeAndTuple(a, t);
            valueCounts.putIfAbsent(v, 0);
            valueCounts.put(v, valueCounts.get(v) + 1);
        }

        int[] counts = new int[valueCounts.size()];

        int i = 0;
        for (int c : valueCounts.values()) {
            counts[i] = c;
            i++;
        }

        return counts;
    }

    /**
     * Computes the power set of a set of items
     * @param originalSet The original set of items
     * @param <T> The type of the items
     * @return The power set of the original set
     */
    public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        HashSet<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
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
    public static <T> Set<Set<T>> subsetsOfSize(Set<T> originalSet, int k) {
        Set<Set<T>> filteredSets = new HashSet<>();
        for (Set<T> set : powerSet(originalSet)) {
            if (set.size() <= k && set.size() > 0) {
                filteredSets.add(set);
            }
        }
        return filteredSets;
    }

    @Override
    public Iterator<Tuple> iterator() {
        return new TupleCollectionIterator();
    }

    class TupleCollectionIterator implements Iterator<Tuple> {
        int currentTuple = 0;

        @Override
        public boolean hasNext() {
            return currentTuple < TupleCollection.this.tupleCount();
        }

        @Override
        public Tuple next() {
            if (hasNext()) {
                int toReturn = currentTuple;
                currentTuple++;
                return TupleCollection.this.getTuple(toReturn);
            }
            throw new NoSuchElementException();
        }

    }

}
