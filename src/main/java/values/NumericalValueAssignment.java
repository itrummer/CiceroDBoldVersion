package values;

/**
 */
public class NumericalValueAssignment {
    String attribute;
    Value lowerBound;
    Value upperBound;

    public NumericalValueAssignment(String attribute, Value lowerBound, Value upperBound) {
        this.attribute = attribute;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
}
