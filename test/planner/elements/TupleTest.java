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

    public void testTupleConstructor() {
        Tuple t = new Tuple(new ArrayList<String>());
        t.addValueAssignment("name", new Value("Mark Bryan"));
        t.addValueAssignment("netID", new Value("mab539"));
        t.addValueAssignment("age", new Value(21));

        assertTrue(t.valueForAttribute("name").equals(new Value("Mark Bryan")));
        assertTrue(t.valueForAttribute("netID").equals(new Value("mab539")));
        assertTrue(t.valueForAttribute("age").equals(new Value(21)));
    }

}