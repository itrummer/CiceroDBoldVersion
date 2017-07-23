package data;

import junit.framework.TestCase;
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
        TupleCollection tuples = connector.buildTupleCollectionFromCSV(testCSV, header);

        assertEquals(tuples.tupleCount(), 3);
    }

    public void testCSVConnectorParseIntegerType() throws Exception {
        String testCSV = "Mark,21,Edina\n";
        CSVConnector connector = new CSVConnector();

        String[] header1 = new String[]{"name:STRING", "age:INTEGER", "hometown:STRING"};
        TupleCollection tuples1 = connector.buildTupleCollectionFromCSV(testCSV, header1);

        Value ageForMark1 = tuples1.getTuple(0).valueForAttribute("age");
        assertTrue(ageForMark1.equals(new Value(21)));

        String[] header2 = new String[]{"name:STRING", "age:INTEGER", "hometown:STRING"};
        TupleCollection tuples2 = connector.buildTupleCollectionFromCSV(testCSV, header2);

        Value ageForMark2 = tuples2.getTuple(0).valueForAttribute("age");
        assertTrue(ageForMark2.equals(new Value(21)));
    }

    public void testBuildTupleCollectionFromCSVStringWithSomeTypes() throws Exception {
        String[] header = new String[]{"name:STRING", "age:INTEGER", "hometown"};
        String testCSV =
                "Mark,21,Edina\n" +
                        "John,24,Edina\n" +
                        "Jack,21,New York City\n";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV(testCSV, header);

        assertEquals(tuples.tupleCount(), 3);
    }
}
