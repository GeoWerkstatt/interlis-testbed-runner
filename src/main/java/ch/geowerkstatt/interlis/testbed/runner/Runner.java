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
    public void run() {
        LOGGER.info("Starting validation of testbed at " + options.basePath());

        if (!validateSuccessfulData()) {
            LOGGER.error("Validation of base data failed.");
            return;
        }

        LOGGER.info("Validation of testbed completed.");
    }

    private boolean validateSuccessfulData() {
        var filePath = options.baseDataFilePath();
        LOGGER.info("Validating base data file " + filePath);
        return validator.validate(filePath);
    }
}
