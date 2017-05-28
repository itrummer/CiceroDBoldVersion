package planner.elements;

import junit.framework.TestCase;

import java.util.*;

import static org.junit.Assert.*;

public class TupleCollectionTest extends TestCase {

    public TupleCollection tupleCollection1() {
        List<String> attributes = new ArrayList<>();
        attributes.add("a0");
        attributes.add("a1");
        attributes.add("a2");
        TupleCollection tC = new TupleCollection(attributes);

        Tuple t1 = new Tuple(attributes);
        t1.addValueAssignment("a0", new Value(1));
        t1.addValueAssignment("a1", new Value("stringValue1"));
        t1.addValueAssignment("a2", new Value(2.5));

        Tuple t2 = new Tuple(attributes);
        t2.addValueAssignment("a0", new Value(2));
        t2.addValueAssignment("a1", new Value("stringValue1"));
        t2.addValueAssignment("a2", new Value(3.0));

        Tuple t3 = new Tuple(attributes);
        t3.addValueAssignment("a0", new Value(3));
        t3.addValueAssignment("a1", new Value("stringValue2"));
        t3.addValueAssignment("a2", new Value(3.5));

        tC.addTuple(t1);
        tC.addTuple(t2);
        tC.addTuple(t3);

        return tC;
    }

    public TupleCollection tupleCollectionWith3Attributes5Tuples() {
        List<String> attributes = new ArrayList<>();
        attributes.add("attribute1");
        attributes.add("attribute2");
        attributes.add("attribute3");
        TupleCollection tupleCollection = new TupleCollection(attributes);

        Tuple t1 = new Tuple(attributes);
        t1.addValueAssignment("attribute1", new Value(1));
        t1.addValueAssignment("attribute2", new Value("stringValue1"));
        t1.addValueAssignment("attribute3", new Value(2.5));

        Tuple t2 = new Tuple(attributes);
        t2.addValueAssignment("attribute1", new Value(2));
        t2.addValueAssignment("attribute2", new Value("stringValue1"));
        t2.addValueAssignment("attribute3", new Value(3.0));

        Tuple t3 = new Tuple(attributes);
        t3.addValueAssignment("attribute1", new Value(3));
        t3.addValueAssignment("attribute2", new Value("stringValue2"));
        t3.addValueAssignment("attribute3", new Value(3.5));

        Tuple t4 = new Tuple(attributes);
        t4.addValueAssignment("attribute1", new Value(4));
        t4.addValueAssignment("attribute2", new Value("stringValue3"));
        t4.addValueAssignment("attribute3", new Value(1.0));

        Tuple t5 = new Tuple(attributes);
        t5.addValueAssignment("attribute1", new Value(5));
        t5.addValueAssignment("attribute2", new Value("stringValue3"));
        t5.addValueAssignment("attribute3", new Value(1.25));

        tupleCollection.addTuple(t1);
        tupleCollection.addTuple(t2);
        tupleCollection.addTuple(t3);
        tupleCollection.addTuple(t4);
        tupleCollection.addTuple(t5);

        return tupleCollection;
    }

    public void testCandidateAssignments() throws Exception {
        TupleCollection tC = tupleCollection1();

        Map<Integer, Set<ValueDomain>> result = tC.candidateAssignments(2, 2.0);
        for (Integer i : result.keySet()) {
            System.out.println("Value domains for attribute " + i + ":");
            for (ValueDomain d : result.get(i)) {
                System.out.println("\t" + d);
            }
        }
    }

    public void testTupleCollectionIterator() {
        TupleCollection tC = tupleCollection1();

        int i = 0;
        for (Tuple t : tC) {
            i++;
        }
        assertEquals(tC.tupleCount(), i);

        i = 0;
        Iterator<Tuple> tuples = tC.iterator();
        while (tuples.hasNext()) {
            Tuple t = tuples.next();
            i++;
        }
        assertEquals(tC.tupleCount(), i);
    }

    public void testTupleCount() {
        TupleCollection tupleCollection = tupleCollectionWith3Attributes5Tuples();
        assertEquals(5, tupleCollection.tupleCount());
    }

    public void testAttributeCount() {
        TupleCollection tupleCollection = tupleCollectionWith3Attributes5Tuples();
        assertEquals(3, tupleCollection.attributeCount());
    }

    public void testAttributeCost() {
        List<String> attributes = new ArrayList<>();
        attributes.add("id");
        attributes.add("name");
        attributes.add("storage_amount");
        TupleCollection tupleCollection = new TupleCollection(attributes);

        assertEquals(2, tupleCollection.costForAttribute(0));
        assertEquals(4, tupleCollection.costForAttribute(1));
        assertEquals(14, tupleCollection.costForAttribute(2));
    }

    public void testEntropy() {
        List<String> attributes = new ArrayList<>();
        attributes.add("a");
        attributes.add("b");
        attributes.add("c");
        attributes.add("d");
        TupleCollection tuples = new TupleCollection(attributes);

        Tuple t1 = new Tuple(attributes);
        t1.addValueAssignment("a", new Value("a1"));
        t1.addValueAssignment("b", new Value("b1"));
        t1.addValueAssignment("c", new Value("c1"));
        t1.addValueAssignment("d", new Value(1));

        Tuple t2 = new Tuple(attributes);
        t2.addValueAssignment("a", new Value("a2"));
        t2.addValueAssignment("b", new Value("b2"));
        t2.addValueAssignment("c", new Value("c2"));
        t2.addValueAssignment("d", new Value(2));

        tuples.addTuple(t1);
        tuples.addTuple(t2);

        System.out.println(tuples.entropy(2.0));
    }

}