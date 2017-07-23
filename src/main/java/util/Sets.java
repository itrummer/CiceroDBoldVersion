package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility methods for Sets
 */
public class Sets {

    /**
     * Constructs the power set of a given input set
     *
     * @param originalSet The original set from which to construct a power set
     * @param <T> The inner type of the original set
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
     *
     * @param originalSet The set of objects from which to create subsets
     * @param k The maximum allowed size of a subset
     * @param <T> The type of the objects in the Set
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
}
