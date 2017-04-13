package values;

/**
 */
public abstract class NumericalValue extends Value {
    /**
     *
     * @return The value formatted as a double
     */
    public abstract Double getLinearProgrammingCoefficient();

    @Override
    public boolean equals(Object obj) {
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }
        return equals(((NumericalValue) obj).getLinearProgrammingCoefficient());
    }

    @Override
    public int hashCode() {
        return getLinearProgrammingCoefficient().hashCode();
    }

    @Override
    public boolean isCategorical() {
        return false;
    }
}
