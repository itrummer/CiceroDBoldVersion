package planner.elements;

import junit.framework.TestCase;
import planner.elements.CategoricalValueDomain;
import planner.elements.Value;

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

}