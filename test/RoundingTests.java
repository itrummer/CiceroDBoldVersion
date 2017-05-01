import junit.framework.TestCase;
import planner.elements.Value;

import java.util.ArrayList;

/**
 */
public class RoundingTests extends TestCase {

    public void testRoundingIntegerValues() {
        Value v1 = new Value(123);
        ArrayList<Value> values = v1.roundedValues();
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
        ArrayList<Value> values = v1.roundedValues();
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
        ArrayList<Value> values = v1.roundedValues();
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
