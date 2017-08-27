package planning.planners;

import planning.elements.Tuple;
import planning.elements.TupleCollection;
import planning.elements.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience constructors for test data sets
 */
public class TestTupleCollections {

    public static TupleCollection testCollection1() {
        List<String> atts = new ArrayList<>();
        atts.add("restaurant");
        atts.add("price");
        atts.add("cuisine");

        TupleCollection tuples = new TupleCollection(atts, "Restaurants");

        Tuple t1 = new Tuple(atts);
        t1.addValueAssignment("restaurant", new Value("Daniel's Cafe"));
        t1.addValueAssignment("price", new Value("low"));
        t1.addValueAssignment("cuisine", new Value("American"));

        Tuple t2 = new Tuple(atts);
        t2.addValueAssignment("restaurant", new Value("Chipotle"));
        t2.addValueAssignment("price", new Value("medium"));
        t2.addValueAssignment("cuisine", new Value("Mexican"));

        Tuple t3 = new Tuple(atts);
        t3.addValueAssignment("restaurant", new Value("The Bird"));
        t3.addValueAssignment("price", new Value("medium"));
        t3.addValueAssignment("cuisine", new Value("Bar"));

        tuples.addTuple(t1);
        tuples.addTuple(t2);
        tuples.addTuple(t3);

        return tuples;
    }

}
