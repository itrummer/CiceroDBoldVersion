package data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import planning.elements.Tuple;
import planning.elements.TupleCollection;
import planning.elements.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements parsing of CSV data to a TupleCollection.
 *
 * TODO: add better handling of data types when parsing CSVRecords. This can be done in a few ways.
 * One idea is to specify types similar to SQL (INTEGER, DOUBLE, FLOAT, STRING, etc.) and require
 * that these be included in the header names and separated by a delimiter such as ':' (option 1)
 * or that a special array of types be passed in along with the header names (option 2).
 * For example:
 *
 * Option 1:
 * header = ["name:STRING", "age:INTEGER"]
 *
 * Option 2:
 * headerNames = ["name", "age"]
 * headerTypes = ["STRING", "INTEGER"]
 *
 */
public class CSVConnector {
    public CSVConnector() {
        // TODO: specify CSV configuration here or allow configuration to be overridden in constructor
    }

    public TupleCollection buildTupleCollectionFromCSV(String csv, String... header) throws IOException {
        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader(header));

        List<String> attributes = Arrays.asList(header);
        TupleCollection tupleCollection = new TupleCollection(attributes);

        for (CSVRecord record : parser.getRecords()) {
            Tuple t = new Tuple(attributes);
            for (String att : header) {
                t.addValueAssignment(att, new Value(record.get(att)));
            }
            tupleCollection.addTuple(t);
        }

        return tupleCollection;
    }
}
