package db;

import junit.framework.TestCase;
import planner.elements.Value;

/**
 * Unit tests for a Tuple
 */
public class TupleTest extends TestCase {
    public void testTupleConstructor() {
        Tuple t = new Tuple();
        t.addValueAssignment("name", new Value("Mark Bryan"));
        t.addValueAssignment("netID", new Value("mab539"));
        t.addValueAssignment("age", new Value(21));

        assertTrue(t.valueForAttribute("name").equals(new Value("Mark Bryan")));
        assertTrue(t.valueForAttribute("netID").equals(new Value("mab539")));
        assertTrue(t.valueForAttribute("age").equals(new Value(21)));
    }
}