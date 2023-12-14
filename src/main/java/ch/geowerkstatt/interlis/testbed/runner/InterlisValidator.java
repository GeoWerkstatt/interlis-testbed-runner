package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public final class InterlisValidator implements Validator {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CONSTRAINT_ERROR_PREFIX = "Error: ";

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
    public boolean validate(Path filePath, Path logFile) throws ValidatorException {
        LOGGER.info("Validating " + filePath + " with log file " + logFile);
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

    @Override
    public boolean containsConstraintError(Path logFile, String constraintName) throws ValidatorException {
        var constraintNameWithWordBoundaries = Pattern.compile("\\b" + Pattern.quote(constraintName) + "\\b");

        try (var lines = Files.lines(logFile)) {
            return lines.anyMatch(line -> {
                if (line.startsWith(CONSTRAINT_ERROR_PREFIX) && constraintNameWithWordBoundaries.matcher(line).find()) {
                    LOGGER.info("Found expected error for constraint " + constraintName + " in log file: " + line);
                    return true;
                }
                return false;
            });
        } catch (IOException e) {
            throw new ValidatorException(e);
        }
    }
}
