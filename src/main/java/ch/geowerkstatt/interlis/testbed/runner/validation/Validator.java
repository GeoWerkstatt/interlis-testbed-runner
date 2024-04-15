package ch.geowerkstatt.interlis.testbed.runner.validation;

import java.nio.file.Path;

public interface Validator {
    /**
     * Validates the given file.
     *
     * @param filePath the path to the file to validate.
     * @param logFile  the path to the log file.
     * @return {@code true} if the validation was successful, {@code false} otherwise.
     * @throws ValidatorException if the validation could not be performed.
     */
    boolean validate(Path filePath, Path logFile) throws ValidatorException;

    /**
     * Checks if the log file contains at least one error for the provided constraint.
     *
     * @param logFile        the path to the log file.
     * @param constraintName the fully qualified name of the constraint to check.
     * @return {@code true} if the log file contains an error for the constraint, {@code false} otherwise.
     * @throws ValidatorException if an unexpected error occurred.
     */
    default boolean containsConstraintError(Path logFile, String constraintName) throws ValidatorException {
        return IliValidatorLogParser.containsConstraintError(logFile, constraintName);
    }
}
