package values;

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

    @Override
    public boolean isCategorical() {
        return true;
    }
}
