package values;

import values.Value;

/**
 */
public abstract class CategoricalValue extends Value {
    @Override
    public boolean equals(Object obj) {
        return toSpeechText().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return toSpeechText().hashCode();
    }
}
