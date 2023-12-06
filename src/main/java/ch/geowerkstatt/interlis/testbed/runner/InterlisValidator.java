package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public final class InterlisValidator implements Validator {
    private static final Logger LOGGER = LogManager.getLogger();

    private final TestOptions options;

    /**
     * Creates a new instance of the InterlisValidator class.
     *
     * @param options the test options.
     */
    public InterlisValidator(TestOptions options) {
        this.options = options;

        LOGGER.info("Using ilivalidator at " + options.ilivalidatorPath());
    }

    @Override
    public boolean validate(Path filePath) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
