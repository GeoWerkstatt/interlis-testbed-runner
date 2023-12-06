package ch.geowerkstatt.interlis.testbed.runner;

import java.nio.file.Path;

public interface Validator {
    /**
     * Validates the given file.
     *
     * @param filePath the path to the file to validate.
     * @return true if the validation was successful, false otherwise.
     */
    boolean validate(Path filePath);
}
