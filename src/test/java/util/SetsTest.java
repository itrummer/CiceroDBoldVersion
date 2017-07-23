package util;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

public class SetsTest extends TestCase {
    public void testPowerSetOfEmptySet() {
        Set<Integer> emptySet = new HashSet<>();
        Set<Set<Integer>> powerSet = Sets.powerSet(emptySet);

        // power set includes only the empty set
        assertEquals(1, powerSet.size());
        assertTrue(powerSet.contains(emptySet));
    }

    public void testPowerSetForSingleItemSet() {
        Set<Integer> testSet = new HashSet<>();
        testSet.add(1);

        Set<Set<Integer>> powerSet = Sets.powerSet(testSet);
        assertEquals(2, powerSet.size());

        Set<Integer> emptySet = new HashSet<>();
        Set<Integer> setWithInteger1 = new HashSet<>();
        assertTrue(powerSet.contains(emptySet));
        assertTrue(powerSet.contains(setWithInteger1));
    }

    // TODO: unit tests for subsetOfSize() method

}
