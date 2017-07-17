package planning.elements;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.List;

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

    public void testRoundingIntegerValues() {
        Value v1 = new Value(123);
        List<Value> values = v1.roundedValues();
        assertTrue(values.get(0).equals(new Value(100)));
        assertTrue(values.get(1).equals(new Value(200)));

        Value v2 = new Value(1999);
        values = v2.roundedValues();
        assertTrue(values.get(0).equals(new Value(1000)));
        assertTrue(values.get(1).equals(new Value(2000)));

        Value v3 = new Value(9999);
        values = v3.roundedValues();
        assertTrue(values.get(0).equals(new Value(9000)));
        assertTrue(values.get(1).equals(new Value(10000)));
    }

    public void testRoundingDoubleValues() {
        Value v1 = new Value(123.0);
        List<Value> values = v1.roundedValues();
        assertTrue(values.get(0).equals(new Value(100.0)));
        assertTrue(values.get(1).equals(new Value(200.0)));

        Value v2 = new Value(1999.9);
        values = v2.roundedValues();
        assertTrue(values.get(0).equals(new Value(1000.0)));
        assertTrue(values.get(1).equals(new Value(2000.0)));

        Value v3 = new Value(9999.3);
        values = v3.roundedValues();
        assertTrue(values.get(0).equals(new Value(9000.0)));
        assertTrue(values.get(1).equals(new Value(10000.0)));
    }

    public void testRoundingFloatValues() {
        Value v1 = new Value(new Float(123.0));
        List<Value> values = v1.roundedValues();
        assertTrue(values.get(0).equals(new Value(new Float(100.0))));
        assertTrue(values.get(1).equals(new Value(new Float(200.0))));

        Value v2 = new Value(new Float(1999.9));
        values = v2.roundedValues();
        assertTrue(values.get(0).equals(new Value(new Float(1000.0))));
        assertTrue(values.get(1).equals(new Value(new Float(2000.0))));

        Value v3 = new Value(new Float(9999.3));
        values = v3.roundedValues();
        assertTrue(values.get(0).equals(new Value(new Float(9000.0))));
        assertTrue(values.get(1).equals(new Value(new Float(10000.0))));
    }

}