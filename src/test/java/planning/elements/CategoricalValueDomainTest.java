package planning.elements;

import junit.framework.TestCase;

/**
 * Unit tests for CategoricalValueDomain
 */
public class CategoricalValueDomainTest extends TestCase {
    public void testContains() throws Exception {
        CategoricalValueDomain domain = new CategoricalValueDomain("testValue");
        domain.addValueToDomain(new Value("value1"));
        domain.addValueToDomain(new Value("value2"));
        domain.addValueToDomain(new Value("value3"));

        assertTrue(domain.contains(new Value("value1")));
        assertTrue(domain.contains(new Value("value2")));
        assertTrue(domain.contains(new Value("value3")));

        assertFalse(domain.contains(new Value("value4")));
        assertFalse(domain.contains(new Value("value5")));
    }

    public void testEquals() {
        CategoricalValueDomain c1 = new CategoricalValueDomain("a1", new Value("value1"));
        CategoricalValueDomain c2 = new CategoricalValueDomain("a1", new Value("value1"));
        assertTrue(c1.equals(c2));

        CategoricalValueDomain c3 = new CategoricalValueDomain("a1", new Value("value2"));
        assertFalse(c1.equals(c3));

        CategoricalValueDomain c4 = new CategoricalValueDomain("a1");
        c4.addValueToDomain(new Value("value1"));
        c4.addValueToDomain(new Value("value2"));
        assertFalse(c1.equals(c3));
        assertFalse(c1.equals(c4));

        CategoricalValueDomain c5 = new CategoricalValueDomain("a1");
        c5.addValueToDomain(new Value("value1"));
        c5.addValueToDomain(new Value("value2"));
    }
}