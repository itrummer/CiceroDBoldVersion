package data;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import planning.elements.Tuple;
import planning.elements.TupleCollection;
import planning.elements.Value;

import java.io.IOException;
import java.util.*;

/**
 * Implements parsing of CSV data to a TupleCollection.
 */
public class CSVConnector {

    private Value parseValue(String v, String type) throws Exception {
        switch (type.toUpperCase()) {
            case "INT":
            case "INTEGER":
                return new Value(Integer.parseInt(v));
            case "DOUBLE":
                return new Value(Double.parseDouble(v));
            case "FLOAT":
                return new Value(Float.parseFloat(v));
            default:
                return new Value(v);
        }
    }

    public TupleCollection buildTupleCollectionFromCSV(String tuplesClassName, String csv, String... header) throws Exception {
        String[] attributes = new String[header.length];
        Map<String, String> typeMap = new HashMap<>();

        for (int i = 0; i < header.length; i++) {
            String h = header[i]; // 'name:INTEGER'
            int idx = h.indexOf(':');
            if (idx != -1) {
                String att = h.substring(0, idx);
                String type = h.substring(idx + 1);
                attributes[i] = att;
                typeMap.put(att, type);
            } else {
                // default to STRING value if no type specified
                attributes[i] = h;
                typeMap.put(h, "STRING");
            }
        }

        CSVParser parser = CSVParser.parse(csv, CSVFormat.DEFAULT.withHeader(attributes));
        TupleCollection tupleCollection = new TupleCollection(Arrays.asList(attributes), tuplesClassName);

        for (CSVRecord record : parser.getRecords()) {
            Tuple t = new Tuple(Arrays.asList(attributes));
            for (String att : attributes) {
                t.addValueAssignment(att, parseValue(record.get(att), typeMap.get(att)));
            }
            tupleCollection.addTuple(t);
        }

        return tupleCollection;
    }
}
