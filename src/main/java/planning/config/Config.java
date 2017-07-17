package planning.config;


public class Config {
    Integer maxAllowableContextSize; // mS
    Double maxAllowableNumericalDomainWidth; // mW
    Integer maxAllowableCategoricalDomainSize; // mC
    Integer timeout; // timeout before defaulting to a naive result
    Double epsilon; // approximation value for the FANTOM algorithm

    public Config() {

    }

    public Integer getMaxAllowableContextSize() {
        return maxAllowableContextSize != null ? maxAllowableContextSize : 2;
    }

    public void setMaxAllowableContextSize(int maxAllowableContextSize) {
        this.maxAllowableContextSize = maxAllowableContextSize;
    }

    public Double getMaxAllowableNumericalDomainWidth() {
        return maxAllowableNumericalDomainWidth != null ? maxAllowableNumericalDomainWidth : 2.0;
    }

    public void setMaxAllowableNumericalDomainWidth(double maxAllowableNumericalDomainWidth) {
        this.maxAllowableNumericalDomainWidth = maxAllowableNumericalDomainWidth;
    }

    public Integer getMaxAllowableCategoricalDomainSize() {
        return maxAllowableCategoricalDomainSize != null ? maxAllowableCategoricalDomainSize : 2;
    }

    public void setMaxAllowableCategoricalDomainSize(int maxAllowableCategoricalDomainSize) {
        this.maxAllowableCategoricalDomainSize = maxAllowableCategoricalDomainSize;
    }

    public Integer getTimeout() {
        return timeout != null ? timeout : 120;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Double getEpsilon() {
        return epsilon != null ? epsilon : 0.1;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

}
