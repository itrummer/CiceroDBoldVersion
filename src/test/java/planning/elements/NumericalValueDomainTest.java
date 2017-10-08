package planning.elements;

import junit.framework.TestCase;

/**
 * Unit tests for NumericalValueDomain
 */
public class NumericalValueDomainTest extends TestCase {
    public void testContains() throws Exception {
        Value lowerBound = new Value(1);
        Value upperBound = new Value(10);
        NumericalValueDomain domain = new NumericalValueDomain("testAttribute", lowerBound, upperBound);

        Value v1 = new Value(1);
        Value v2 = new Value(2);
        Value v3 = new Value(6);
        Value v4 = new Value(10);

        assertTrue(domain.contains(v1));
        assertTrue(domain.contains(v2));
        assertTrue(domain.contains(v3));
        assertTrue(domain.contains(v4));

        Value v5 = new Value(11);
        Value v6 = new Value(0);

        assertFalse(domain.contains(v5));
        assertFalse(domain.contains(v6));
    }

    public void testContainsAlternateConstructor() throws Exception {
        Value bound1 = new Value(1);
        Value bound2 = new Value(10);
        NumericalValueDomain domain = new NumericalValueDomain("testAttribute", bound2, bound1);

        Value v1 = new Value(1);
        Value v2 = new Value(2);
        Value v3 = new Value(6);
        Value v4 = new Value(10);

        assertTrue(domain.contains(v1));
        assertTrue(domain.contains(v2));
        assertTrue(domain.contains(v3));
        assertTrue(domain.contains(v4));

        Value v5 = new Value(11);
        Value v6 = new Value(0);

        assertFalse(domain.contains(v5));
        assertFalse(domain.contains(v6));
    }

    public void testEquals() {
        NumericalValueDomain domain1 = new NumericalValueDomain("a0", new Value(1), new Value(2));
        NumericalValueDomain domain2 = new NumericalValueDomain("a0", new Value(1), new Value(2));
        assertTrue(domain1.equals(domain2));

        NumericalValueDomain domain3 = new NumericalValueDomain("a1", new Value(1), new Value(3));
        assertFalse(domain1.equals(domain3));

        NumericalValueDomain domain4 = new NumericalValueDomain("a0", new Value(0), new Value(0));
        assertFalse(domain1.equals(domain4));

        NumericalValueDomain domain5 = new NumericalValueDomain("a0", new Value(0), new Value(0));
        assertTrue(domain4.equals(domain5));

        NumericalValueDomain domain6 = new NumericalValueDomain("a0", new Value(0));
        assertTrue(domain4.equals(domain6));

        NumericalValueDomain domain7 = new NumericalValueDomain("a0", new Value(2), new Value(1));
        assertTrue(domain1.equals(domain7));
    }
}