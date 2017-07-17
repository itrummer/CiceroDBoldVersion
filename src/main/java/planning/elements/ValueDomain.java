package planning.elements;

import planning.Speakable;

/**
 * Abstract representation of a domain of values.
 */
public abstract class ValueDomain implements Speakable {
    String attribute;

    public String getAttribute() {
        return attribute;
    }

    public abstract boolean contains(Value v);
    public abstract boolean isCategorical();
    public abstract boolean isNumerical();
}
