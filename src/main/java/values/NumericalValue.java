package values;

/**
 */
public abstract class NumericalValue extends Value implements Comparable<NumericalValue> {
    /**
     *
     * @return The value formatted as a double
     */
    public abstract Double getLinearProgrammingCoefficient();

    public int compareTo(NumericalValue o) {
        return getLinearProgrammingCoefficient().compareTo(o.getLinearProgrammingCoefficient());
    }

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
