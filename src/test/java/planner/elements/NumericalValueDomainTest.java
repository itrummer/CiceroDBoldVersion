package planner.elements;

import junit.framework.TestCase;
import planner.elements.NumericalValueDomain;
import planner.elements.Value;

/**
 * Unit tests for NumericalValueDomain
 */
public class NumericalValueDomainTest extends TestCase {
    public void testContains() throws Exception {
        Value lowerBound = new Value(1);
        Value upperBound = new Value(10);
        NumericalValueDomain domain = new NumericalValueDomain("testAttribute", lowerBound, upperBound);

        // should contain these values
        Value v1 = new Value(1);
        Value v2 = new Value(2);
        Value v3 = new Value(6);
        Value v4 = new Value(10);

        assertTrue(domain.contains(v1));
        assertTrue(domain.contains(v2));
        assertTrue(domain.contains(v3));
        assertTrue(domain.contains(v4));

        // should not contain these values
        Value v5 = new Value(11);
        Value v6 = new Value(0);

        assertFalse(domain.contains(v5));
        assertFalse(domain.contains(v6));
    }

    public void testContainsAlternateConstructor() throws Exception {
        Value bound1 = new Value(1);
        Value bound2 = new Value(10);
        NumericalValueDomain domain = new NumericalValueDomain("testAttribute", bound1, bound2);

        // should contain these values
        Value v1 = new Value(1);
        Value v2 = new Value(2);
        Value v3 = new Value(6);
        Value v4 = new Value(10);

        assertTrue(domain.contains(v1));
        assertTrue(domain.contains(v2));
        assertTrue(domain.contains(v3));
        assertTrue(domain.contains(v4));

        // should not contain these values
        Value v5 = new Value(11);
        Value v6 = new Value(0);

        assertFalse(domain.contains(v5));
        assertFalse(domain.contains(v6));
    }

}