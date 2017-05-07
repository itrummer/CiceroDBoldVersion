package planner.elements;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by mabryan on 5/7/17.
 */
public class TupleCollectionTest {

    @Test
    public void testCandidateAssignments() throws Exception {
        ArrayList<String> attributes = new ArrayList<>();
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

        HashMap<Integer, HashSet<ValueDomain>> result = tC.candidateAssignments(2, 2.0);
        for (Integer i : result.keySet()) {
            System.out.println("Value domains for attribute " + i + ":");
            for (ValueDomain d : result.get(i)) {
                System.out.println("\t" + d);
            }
        }
    }

}