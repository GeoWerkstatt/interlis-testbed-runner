package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
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
    public boolean validate(Path filePath) throws ValidatorException {
        var relativePath = options.basePath().relativize(filePath.getParent());
        var logDirectory = options.outputPath().resolve(relativePath);

        var filenameWithoutExtension = StringUtils.getFilenameWithoutExtension(filePath.getFileName().toString());
        var logFile = logDirectory.resolve(filenameWithoutExtension + ".log");

        try {
            Files.createDirectories(logDirectory);

            var processBuilder = new ProcessBuilder()
                    .command(
                            "java", "-jar", options.ilivalidatorPath().toString(),
                            "--log", logFile.toString(),
                            filePath.toString())
                    .directory(options.basePath().toFile());

            var process = processBuilder.start();
            var exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("Validation of " + filePath + " completed successfully.");
                return true;
            } else {
                LOGGER.error("Validation of " + filePath + " failed with exit code " + exitCode + ". See " + logFile + " for details.");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            throw new ValidatorException(e);
        }
    }
}
