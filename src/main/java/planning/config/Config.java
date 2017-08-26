package planning.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    final static Logger logger = LoggerFactory.getLogger(Config.class);

    Integer maxAllowableContextSize; // mS
    Double maxAllowableNumericalDomainWidth; // mW
    Integer maxAllowableCategoricalDomainSize; // mC
    Integer timeout; // timeout before defaulting to a naive result, seconds
    Double epsilon; // approximation value for the FANTOM algorithm

    static final Integer DEFAULT_MAX_ALLOWABLE_CONTEXT_SIZE = 2;
    static final Double DEFAULT_MAX_ALLOWABLE_NUMERICAL_DOMAIN_WIDTH = 2.0;
    static final Integer DEFAULT_MAX_ALLOWABLE_CATEGORICAL_DOMAIN_SIZE = 2;
    static final Integer DEFAULT_TIMEOUT = 120;
    static final Double DEFAULT_EPSILON = 0.1;

    public Config() {

    }

    public Integer getMaxAllowableContextSize() {
        if (maxAllowableContextSize == null) {
            logger.warn(String.format("Using default value %d for max allowable context size", DEFAULT_MAX_ALLOWABLE_CONTEXT_SIZE));
            return DEFAULT_MAX_ALLOWABLE_CONTEXT_SIZE;
        }
        return maxAllowableContextSize;
    }

    public void setMaxAllowableContextSize(int maxAllowableContextSize) throws InvalidConfigValueException {
        if (maxAllowableContextSize < 0) {
            throw new InvalidConfigValueException(
                    String.format("Max allowable context size must be non-negative. Received value: ", maxAllowableContextSize)
            );
        }
        this.maxAllowableContextSize = maxAllowableContextSize;
    }

    public Double getMaxAllowableNumericalDomainWidth() {
        if (maxAllowableNumericalDomainWidth == null) {
            logger.warn(String.format("Using default value %f for max allowable numerical domain width",
                    DEFAULT_MAX_ALLOWABLE_NUMERICAL_DOMAIN_WIDTH));
            return DEFAULT_MAX_ALLOWABLE_NUMERICAL_DOMAIN_WIDTH;
        }
        return maxAllowableNumericalDomainWidth;
    }

    public void setMaxAllowableNumericalDomainWidth(double maxAllowableNumericalDomainWidth) throws InvalidConfigValueException {
        if (maxAllowableNumericalDomainWidth < 0) {
            throw new InvalidConfigValueException(
                    String.format("Max allowable numerical domain width must be non-negative", maxAllowableNumericalDomainWidth)
            );
        }
        this.maxAllowableNumericalDomainWidth = maxAllowableNumericalDomainWidth;
    }

    public Integer getMaxAllowableCategoricalDomainSize() {
        if (maxAllowableCategoricalDomainSize == null) {
            logger.warn(String.format("Using default value %d for max allowable categorical domain size",
                    DEFAULT_MAX_ALLOWABLE_CATEGORICAL_DOMAIN_SIZE));
            return DEFAULT_MAX_ALLOWABLE_CATEGORICAL_DOMAIN_SIZE;
        }
        return maxAllowableCategoricalDomainSize;
    }

    public void setMaxAllowableCategoricalDomainSize(int maxAllowableCategoricalDomainSize) throws InvalidConfigValueException {
        if (maxAllowableCategoricalDomainSize < 0) {
            throw new InvalidConfigValueException(
                    String.format("Max allowable categorical domain size must be non-negative. Received value %d", maxAllowableCategoricalDomainSize)
            );
        }
        this.maxAllowableCategoricalDomainSize = maxAllowableCategoricalDomainSize;
    }

    public Integer getTimeout() {
        if (timeout == null) {
            logger.warn("Using default value %d for timeout", DEFAULT_TIMEOUT);
            return DEFAULT_TIMEOUT;
        }
        return timeout;
    }

    public void setTimeout(int timeout) throws InvalidConfigValueException {
        if (timeout <= 0) {
            throw new InvalidConfigValueException(String.format("Timeout must be positive. Received value: %d", timeout));
        }
        this.timeout = timeout;
    }

    public Double getEpsilon() {
        if (epsilon == null) {
            logger.warn("Using default value %f for epsilon approximation", DEFAULT_EPSILON);
            return DEFAULT_EPSILON;
        }
        return epsilon;
    }

    public void setEpsilon(double epsilon) throws InvalidConfigValueException {
        if (epsilon <= 0) {
            throw new InvalidConfigValueException(String.format("Epsilon value cannot be negative. Received value %f", epsilon));
        }
        this.epsilon = epsilon;
    }

    /**
     * Exception class representing that an invalid configuration value was set
     */
    public class InvalidConfigValueException extends Exception {
        public InvalidConfigValueException(String message) {
            super(message);
        }
    }
}
