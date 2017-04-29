package planner.elements;

/**
 * Abstract representation of a domain of values.
 */
public abstract class ValueDomain {
    public abstract boolean contains(Value v);
}
