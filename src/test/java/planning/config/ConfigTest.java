package planning.config;

import junit.framework.TestCase;

/**
 * Unit tests for Config
 */
public class ConfigTest extends TestCase {
    Config config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        config = new Config();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        config = null;
    }

    public void testEpsilonMustBePositiveNonZero() {
        boolean thrown = false;
        try {
            config.setEpsilon(0.0);
        } catch (Config.InvalidConfigValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testAcceptableEpsilon() throws Config.InvalidConfigValueException {
        config.setEpsilon(0.1);
    }

    public void testMaxContextSizeMustBeNonNegative() {
        boolean thrown = false;
        try {
            config.setMaxAllowableContextSize(-1);
        } catch (Config.InvalidConfigValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testAcceptableMaxContextSize() throws Config.InvalidConfigValueException {
        config.setMaxAllowableContextSize(0);
        config.setMaxAllowableContextSize(1);
        config.setMaxAllowableContextSize(2);
    }

    public void testMaxCategoricalDomainSizeMustBeNonNegative() {
        boolean thrown = false;
        try {
            config.setMaxAllowableCategoricalDomainSize(-1);
        } catch (Config.InvalidConfigValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testAcceptableMaxCategoricalDomainSize() throws Config.InvalidConfigValueException {
        config.setMaxAllowableCategoricalDomainSize(0);
        config.setMaxAllowableCategoricalDomainSize(1);
        config.setMaxAllowableCategoricalDomainSize(2);
    }

    public void testMaxNumericalDomainWidthMustBeNonNegative() {
        boolean thrown = false;
        try {
            config.setMaxAllowableNumericalDomainWidth(-1.0);
        } catch (Config.InvalidConfigValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testAcceptableNumericalDomainWidth() throws Config.InvalidConfigValueException {
        config.setMaxAllowableNumericalDomainWidth(0.0);
        config.setMaxAllowableNumericalDomainWidth(1.0);
        config.setMaxAllowableNumericalDomainWidth(2.0);
        config.setMaxAllowableNumericalDomainWidth(5.0);
    }

    public void testTimeoutMustBeNonZero() {
        boolean thrown = false;
        try {
            config.setTimeout(0);
        } catch (Config.InvalidConfigValueException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testAcceptableTimeout() throws Config.InvalidConfigValueException {
        config.setTimeout(1);
        config.setTimeout(120);
    }
}
