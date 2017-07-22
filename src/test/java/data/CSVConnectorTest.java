package data;

import junit.framework.TestCase;
import planning.elements.TupleCollection;

public class CSVConnectorTest extends TestCase {

    public void testBuildTupleCollectionFromCSVString() throws Exception {
        String[] header = new String[]{"name", "age", "hometown"};
        String testCSV =
                "Mark,21,Edina\n" +
                "John,24,Edina\n" +
                "Jack,21,New York City\n";
        CSVConnector connector = new CSVConnector();
        TupleCollection tuples = connector.buildTupleCollectionFromCSV(testCSV, header);

        assertEquals(tuples.tupleCount(), 3);

        // TODO: more tests!
    }
}