package ch.geowerkstatt.interlis.testbed.runner.validation;

import ch.geowerkstatt.interlis.testbed.runner.TestOptions;
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

        LOGGER.info("Using ilivalidator at {}", options.ilivalidatorPath());
    }

    @Override
    public boolean validate(Path filePath, Path logFile) throws ValidatorException {
        LOGGER.info("Validating {} with log file {}", filePath, logFile);
        try {
            Files.createDirectories(logFile.getParent());

            var processBuilder = new ProcessBuilder()
                    .command(
                            "java", "-jar", options.ilivalidatorPath().toString(),
                            "--log", logFile.toString(),
                            "--modeldir", options.basePath() + ";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels",
                            filePath.toString())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .directory(options.basePath().toFile());

            var process = processBuilder.start();
            var exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            throw new ValidatorException(e);
        }
    }
}
