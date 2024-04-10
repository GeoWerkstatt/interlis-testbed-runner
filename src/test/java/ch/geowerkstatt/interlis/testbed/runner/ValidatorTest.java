package ch.geowerkstatt.interlis.testbed.runner;

import ch.geowerkstatt.interlis.testbed.runner.validation.InterlisValidator;
import ch.geowerkstatt.interlis.testbed.runner.validation.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ValidatorTest {
    private static final String CONSTRAINT_A_NAME = "ModelA.TopicA.ClassA.ConstraintA";
    private static final String CONSTRAINT_B_NAME = "ModelA.TopicA.ClassA.ConstraintB";
    private static final Path BASE_PATH = Path.of("src/test/data/validator");
    private static final Path LOG_FILE = BASE_PATH.resolve("logfile.log");

    private TestOptions options;

    @BeforeEach
    public void setup() {
        options = new TestOptions(BASE_PATH, Path.of("ilivalidator.jar"));
    }

    @Test
    public void containsConstraintError() throws ValidatorException {
        var validator = new InterlisValidator(options);

        var result = validator.containsConstraintError(LOG_FILE, CONSTRAINT_A_NAME);

        assertTrue(result, "The log file should contain an error for constraint A.");
    }

    @Test
    public void noErrorForValidConstraint() throws ValidatorException, IOException {
        var validator = new InterlisValidator(options);

        var result = validator.containsConstraintError(LOG_FILE, CONSTRAINT_B_NAME);

        assertFalse(result, "The log file should not contain an error for constraint B.");

        try (var lines = Files.lines(LOG_FILE)) {
            var hasConstraintInfo = lines.anyMatch(line -> line.startsWith("Info:") && line.contains(CONSTRAINT_B_NAME));
            assertTrue(hasConstraintInfo, "The log file should contain an info message for the constraint.");
        }
    }
}
