package data;

import junit.framework.TestCase;
import planning.elements.Tuple;
import planning.elements.TupleCollection;
import planning.elements.Value;

public class CSVConnectorTest extends TestCase {

    public void testBuildTupleCollectionFromCSVStringWithoutTypes() throws Exception {
        String[] header = new String[]{"name", "age", "hometown"};
        String testCSV =
                "Mark,21,Edina\n" +
                "John,24,Edina\n" +
                "Jack,21,New York City\n";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV("People", testCSV, header);

        assertEquals(tuples.tupleCount(), 3);
    }

    public void testCSVConnectorParseIntegerType() throws Exception {
        String testCSV = "Mark,21,Edina\n";
        CSVConnector connector = new CSVConnector();

        String[] header1 = new String[]{"name:STRING", "age:INTEGER", "hometown:STRING"};
        TupleCollection tuples1 = connector.buildTupleCollectionFromCSV("People", testCSV, header1);

        Value ageForMark1 = tuples1.getTuple(0).valueForAttribute("age");
        assertTrue(ageForMark1.equals(new Value(21)));

        String[] header2 = new String[]{"name:STRING", "age:INTEGER", "hometown:STRING"};
        TupleCollection tuples2 = connector.buildTupleCollectionFromCSV("People", testCSV, header2);

        Value ageForMark2 = tuples2.getTuple(0).valueForAttribute("age");
        assertTrue(ageForMark2.equals(new Value(21)));
    }

    public void testBuildTupleCollectionFromCSVStringWithSomeTypes() throws Exception {
        String[] header = new String[]{"name:STRING", "age:INTEGER", "hometown"};
        String testCSV = "Mark,21,Edina\n" +
                "John,24,Edina\n" +
                "Jack,21,New York City\n";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV("People", testCSV, header);
        assertEquals(tuples.tupleCount(), 3);
    }

    public void testBuildLargeCSVWithTypes() throws Exception {
        String[] header = new String[]{"restaurant:STRING", "rating:DOUBLE", "price:STRING", "cuisine:STRING"};
        String testCSV = "Carmine's Italian Restaurant,4.3,medium,Italian\n" +
                "Havana Central Times Square,4.2,medium,Cuban\n" +
                "Lillie's Victorian Establishment,4.3,medium,British\n" +
                "Times Square Diner & Grill,4.2,low,Diner\n";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV("Restaurants", testCSV, header);
        assertEquals(4, tuples.tupleCount());

        Tuple t0 = tuples.getTuple(0);
        assertTrue(t0.valueForAttribute("restaurant").equals(new Value("Carmine's Italian Restaurant")));
        assertTrue(t0.valueForAttribute("rating").equals(new Value(4.3)));
        assertTrue(t0.valueForAttribute("price").equals(new Value("medium")));
        assertTrue(t0.valueForAttribute("cuisine").equals(new Value("Italian")));

        Tuple t1 = tuples.getTuple(1);
        assertTrue(t1.valueForAttribute("restaurant").equals(new Value("Havana Central Times Square")));
        assertTrue(t1.valueForAttribute("rating").equals(new Value(4.2)));
        assertTrue(t1.valueForAttribute("price").equals(new Value("medium")));
        assertTrue(t1.valueForAttribute("cuisine").equals(new Value("Cuban")));

        Tuple t2 = tuples.getTuple(2);
        assertTrue(t2.valueForAttribute("restaurant").equals(new Value("Lillie's Victorian Establishment")));
        assertTrue(t2.valueForAttribute("rating").equals(new Value(4.3)));
        assertTrue(t2.valueForAttribute("price").equals(new Value("medium")));
        assertTrue(t2.valueForAttribute("cuisine").equals(new Value("British")));

        Tuple t3 = tuples.getTuple(3);
        assertTrue(t3.valueForAttribute("restaurant").equals(new Value("Times Square Diner & Grill")));
        assertTrue(t3.valueForAttribute("rating").equals(new Value(4.2)));
        assertTrue(t3.valueForAttribute("price").equals(new Value("low")));
        assertTrue(t3.valueForAttribute("cuisine").equals(new Value("Diner")));
    }

    public void testBuildCSVWithoutNewLineAtEnd() throws Exception {
        String[] testHeader = new String[]{"att1:STRING", "att2:INTEGER", "att3:DOUBLE"};
        String testCSV = "testVariable,2,5.4";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV("Entries", testCSV, testHeader);

        assertEquals(1, tuples.tupleCount());

        Tuple t = tuples.getTuple(0);
        assertTrue(t.valueForAttribute("att1").equals(new Value("testVariable")));
        assertTrue(t.valueForAttribute("att2").equals(new Value(2)));
        assertTrue(t.valueForAttribute("att3").equals(new Value(5.4)));
    }

    public void testBuildCSVWithNewLineAtEnd() throws Exception {
        String[] testHeader = new String[]{"att1:STRING", "att2:INTEGER", "att3:DOUBLE"};
        String testCSV = "testVariable,2,5.4\n";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV("Entries", testCSV, testHeader);

        assertEquals(1, tuples.tupleCount());

        Tuple t = tuples.getTuple(0);
        assertTrue(t.valueForAttribute("att1").equals(new Value("testVariable")));
        assertTrue(t.valueForAttribute("att2").equals(new Value(2)));
        assertTrue(t.valueForAttribute("att3").equals(new Value(5.4)));
    }
}
