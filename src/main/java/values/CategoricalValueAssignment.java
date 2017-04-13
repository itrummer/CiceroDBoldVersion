package values;

import java.util.ArrayList;

/**
 * Created by mabryan on 4/13/17.
 */
public class CategoricalValueAssignment {
    String attribute;
    ArrayList<CategoricalValue> values;

    public CategoricalValueAssignment(String attribute, ArrayList<CategoricalValue> values) {
        this.attribute = attribute;
        this.values = values;
    }
}
