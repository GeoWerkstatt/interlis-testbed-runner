package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public final class Runner {
    private static final Logger LOGGER = LogManager.getLogger();

    private final TestOptions options;
    private final Validator validator;
    private Path baseFilePath;

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
     * @return {@code true} if the validation was successful, {@code false} otherwise.
     */
    public boolean run() {
        LOGGER.info("Starting validation of testbed at " + options.basePath());

        try {
            if (!validateBaseData()) {
                return false;
            }
        } catch (ValidatorException e) {
            LOGGER.error("Validation could not run, check the configuration.", e);
            return false;
        }

        LOGGER.info("Validation of testbed completed.");
        return true;
    }

    private boolean validateBaseData() throws ValidatorException {
        try {
            var baseFilePath = options.baseDataFilePath();
            if (baseFilePath.isEmpty()) {
                LOGGER.error("No base data file found.");
                return false;
            }
            this.baseFilePath = baseFilePath.get();
        } catch (IOException e) {
            throw new ValidatorException(e);
        }

        LOGGER.info("Validating base data file " + baseFilePath);
        var filenameWithoutExtension = StringUtils.getFilenameWithoutExtension(baseFilePath.getFileName().toString());
        var logFile = options.resolveOutputFilePath(baseFilePath, filenameWithoutExtension + ".log");

        var valid = validator.validate(baseFilePath, logFile);
        if (valid) {
            LOGGER.info("Validation of " + baseFilePath + " completed successfully.");
        } else {
            LOGGER.error("Validation of " + baseFilePath + " failed. See " + logFile + " for details.");
        }
        return valid;
    }
}
