package values;

/**
 * Created by mabryan on 4/11/17.
 */
public class DoubleValue extends NumericalValue {
    Double value;

    public DoubleValue(Double value) {
        this.value = value;
    }

    @Override
    public String toSpeechText() {
        return value.toString();
    }

    @Override
    public Double getLinearProgrammingCoefficient() {
        return value;
    }
}
