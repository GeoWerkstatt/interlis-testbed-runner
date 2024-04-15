package ch.geowerkstatt.interlis.testbed.runner.validation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Provides static methods that can be used to get information from ilivalidator log files.
 */
public final class IliValidatorLogParser {
    private static final Logger LOGGER = LogManager.getLogger();

    private IliValidatorLogParser() {
    }

    /**
     * Checks if the log file contains at least one error for the provided constraint.
     *
     * @param logFile        the path to the log file.
     * @param constraintName the fully qualified name of the constraint to check.
     * @return {@code true} if the log file contains an error for the constraint, {@code false} otherwise.
     * @throws ValidatorException if an unexpected error such as an {@link IOException} occurred.
     */
    public static boolean containsConstraintError(Path logFile, String constraintName) throws ValidatorException {
        var constraintPattern = Pattern.compile("^Error: .*\\b" + Pattern.quote(constraintName) + "\\b");

        try (var lines = Files.lines(logFile)) {
            return lines.anyMatch(line -> {
                if (constraintPattern.matcher(line).find()) {
                    LOGGER.info("Found expected error for constraint {} in log file: {}", constraintName, line);
                    return true;
                }
                return false;
            });
        } catch (IOException e) {
            throw new ValidatorException(e);
        }
    }
}
