package planner.elements;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class TupleTest extends TestCase {

    public void testGetValueForAttribute() throws Exception {
        List<String> attributes = new ArrayList<>();
        attributes.add("a1");
        attributes.add("a2");
        attributes.add("a3");

        Value v1 = new Value(1);
        Value v2 = new Value(2);
        Value v3 = new Value(3);

        Tuple t1 = new Tuple(attributes);
        t1.addValueAssignment("a1", v1);
        t1.addValueAssignment("a2", v2);
        t1.addValueAssignment("a3", v3);

        assertTrue(v1.equals(t1.valueForAttribute("a1")));
        assertTrue(v2.equals(t1.valueForAttribute("a2")));
        assertTrue(v3.equals(t1.valueForAttribute("a3")));
    }

}