package values;

import java.util.ArrayList;

/**
 */
public class CategoricalValueAssignment {
    String attribute;
    ArrayList<Value> domainValues;

    public CategoricalValueAssignment(String attribute, ArrayList<Value> domainValues) {
        this.attribute = attribute;
        this.domainValues = domainValues;
    }

    public CategoricalValueAssignment(String attribute) {
        this(attribute, new ArrayList<Value>());
    }

    public CategoricalValueAssignment(String attribute, Value firstValue) {
        this(attribute);
        addValueToDomain(firstValue);
    }

    public void addValueToDomain(Value value) {
        addValueToDomain(value);
    }

    public int getDomainSize() {
        return domainValues.size();
    }

    public String getAttribute() {
        return attribute;
    }
}
