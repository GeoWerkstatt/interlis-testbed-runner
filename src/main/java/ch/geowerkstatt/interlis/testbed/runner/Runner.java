package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

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
     * @return {@code true} if the validation was successful, {@code false} otherwise.
     */
    public boolean run() {
        LOGGER.info("Starting validation of testbed at " + options.basePath());

        try {
            if (!validateBaseData()) {
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

    private boolean validateBaseData() throws ValidatorException {
        Optional<Path> filePath;
        try {
            filePath = options.baseDataFilePath();
        } catch (IOException e) {
            throw new ValidatorException(e);
        }

        if (filePath.isEmpty()) {
            LOGGER.error("No base data file found.");
            return false;
        }

        LOGGER.info("Validating base data file " + filePath.get());
        return validator.validate(filePath.get());
    }
}
