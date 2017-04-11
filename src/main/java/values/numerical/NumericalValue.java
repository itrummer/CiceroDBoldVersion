package values.numerical;

import values.Value;

/**
 * Created by mabryan on 4/10/17.
 */
public abstract class NumericalValue extends Value {
    /**
     *
     * @return The value formatted as a double
     */
    abstract double getLinearProgrammingCoefficient();
}
