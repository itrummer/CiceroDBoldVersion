package planner.elements;

/**
 * Abstract representation of a domain of values.
 */
public abstract class ValueDomain {
    String attribute;

    public String getAttribute() {
        return attribute;
    }

    public abstract boolean contains(Value v);
    public abstract boolean isCategorical();
    public abstract boolean isNumerical();
}
