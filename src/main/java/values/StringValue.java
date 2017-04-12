package values;

/**
 * Created by mabryan on 4/10/17.
 */
public class StringValue extends CategoricalValue {
    String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public String toSpeechText() {
        return value;
    }
}
