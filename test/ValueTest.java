import junit.framework.TestCase;
import planner.elements.Value;

import java.util.HashSet;

public class ValueTest extends TestCase {

    public void testIntegerValues() {
        Value v1 = new Value(1);
        Value v3 = new Value(3);
        Value v3Copy = new Value(3);
        Value v10 = new Value(10);

        // compareTo() tests
        assertTrue(v3.compareTo(v3Copy) == 0);
        assertTrue(v3.compareTo(v10) < 0);
        assertTrue(v10.compareTo(v3) > 0);
        assertTrue(v1.compareTo(v1) == 0);

        // equals() tests
        assertTrue(v3.equals(v3Copy));
        assertTrue(v3.equals(v3));
        assertFalse(v1.equals(v10));
        assertFalse(v10.equals(v1));

        // hashCode() tests
        assertTrue(v3.hashCode() == v3Copy.hashCode());
    }

    public void testDoubleValues() {
        Value v1 = new Value(1.0);
        Value v3 = new Value(3.0);
        Value v3Copy = new Value(3.0);
        Value v10 = new Value(10.0);

        // compareTo() tests
        assertTrue(v3.compareTo(v3Copy) == 0);
        assertTrue(v3.compareTo(v10) < 0);
        assertTrue(v10.compareTo(v3) > 0);
        assertTrue(v1.compareTo(v1) == 0);

        // equals() tests
        assertTrue(v3.equals(v3Copy));
        assertTrue(v3.equals(v3));
        assertFalse(v1.equals(v10));
        assertFalse(v10.equals(v1));

        // hashCode() tests
        assertTrue(v3.hashCode() == v3Copy.hashCode());
    }

    public void testStringValues() {
        Value v1 = new Value("string1");
        Value v1Copy = new Value("string1");
        Value v2 = new Value("string2");

        // compareTo() tests
        assertEquals(0, v1.compareTo(v1Copy));

        // equals() tests
        assertTrue(v1.equals(v1Copy));
        assertFalse(v1.equals(v2));

        // hashCode() tests
        assertEquals(v1.hashCode(), v1Copy.hashCode());
    }

    public void testHashSetStringValues() {
        HashSet<Value> testSet = new HashSet<Value>();
        testSet.add(new Value("test string"));
        testSet.add(new Value("test string"));
        assertEquals(1, testSet.size());

        testSet.add(new Value("test string 2"));
        assertEquals(2, testSet.size());
    }

}