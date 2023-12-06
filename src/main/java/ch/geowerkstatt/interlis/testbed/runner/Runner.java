package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Runner {
    private static final Logger LOGGER = LogManager.getLogger();

    private final TestOptions options;
    private final Validator validator;

    /**
     * Creates a new instance of the Runner class.
     *
     * @param options the test options.
     * @param validator the validator to use.
     */
    public Runner(TestOptions options, Validator validator) {
        this.options = options;
        this.validator = validator;
    }

    /**
     * Runs the testbed validation.
     */
    public boolean run() {
        LOGGER.info("Starting validation of testbed at " + options.basePath());

        try {
            if (!validateSuccessfulData()) {
                LOGGER.error("Validation of base data failed.");
                return false;
            }
        } catch (ValidatorException e) {
            LOGGER.error("Validation could not run, check the configuration.", e);
            return false;
        }

        LOGGER.info("Validation of testbed completed.");
        return true;
    }

    private boolean validateSuccessfulData() throws ValidatorException {
        var filePath = options.baseDataFilePath();
        LOGGER.info("Validating base data file " + filePath);
        return validator.validate(filePath);
    }
}
