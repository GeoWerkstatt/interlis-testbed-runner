package ch.geowerkstatt.interlis.testbed.runner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class RunnerTest {
    private static final String BASE_PATH = "src/test/data/testbed";

    private TestLogAppender appender;
    private TestOptions options;

    @BeforeEach
    public void setup() {
        appender = TestLogAppender.registerAppender(Runner.class);
        options = new TestOptions(Path.of(BASE_PATH), Path.of("ilivalidator.jar"));
    }

    @AfterEach
    public void teardown() {
        appender.stop();
        appender.unregister();
    }

    @Test
    public void runValidatesBaseData() throws IOException {
        var validatedFiles = new ArrayList<Path>();

        var runner = new Runner(options, (file, logFile) -> {
            validatedFiles.add(file.toAbsolutePath().normalize());
            return true;
        });

        var runResult = runner.run();

        assertTrue(runResult, "Testbed run should have been successful.");

        var expectedBaseDataFile = Path.of(BASE_PATH, "data.xtf").toAbsolutePath().normalize();
        var baseDataFile = options.baseDataFilePath();
        assertFalse(baseDataFile.isEmpty(), "Base data file should have been found.");
        assertEquals(expectedBaseDataFile, baseDataFile.get());

        var expectedFiles = List.of(expectedBaseDataFile);
        assertIterableEquals(expectedFiles, validatedFiles);

        var errors = appender.getErrorMessages();
        assertEquals(0, errors.size(), "No errors should have been logged.");
    }

    @Test
    public void runLogsValidationError() {
        var runner = new Runner(options, (file, logFile) -> false);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed.");

        var errors = appender.getErrorMessages();
        assertEquals(1, errors.size(), "One error should have been logged.");
        var errorMessage = errors.getFirst();
        assertTrue(
                Pattern.matches("Validation of .*? failed\\..*", errorMessage),
                "Error message should start with 'Validation of <base data file> failed.', actual value: '" + errorMessage + "'."
        );
    }
}
