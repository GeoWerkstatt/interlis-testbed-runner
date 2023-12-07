package ch.geowerkstatt.interlis.testbed.runner;

import ch.geowerkstatt.interlis.testbed.runner.xtf.XtfMerger;
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
    private XtfMerger nullMerger;

    @BeforeEach
    public void setup() {
        appender = TestLogAppender.registerAppender(Runner.class);
        options = new TestOptions(Path.of(BASE_PATH), Path.of("ilivalidator.jar"));
        nullMerger = (baseFile, patchFile, outputFile) -> true;
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
        }, nullMerger);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed without patch files.");

        var errors = appender.getErrorMessages();
        assertIterableEquals(List.of("No patch files found."), errors);

        var expectedBaseDataFile = Path.of(BASE_PATH, "data.xtf").toAbsolutePath().normalize();
        var baseDataFile = options.baseDataFilePath();
        assertFalse(baseDataFile.isEmpty(), "Base data file should have been found.");
        assertEquals(expectedBaseDataFile, baseDataFile.get());

        var expectedFiles = List.of(expectedBaseDataFile);
        assertIterableEquals(expectedFiles, validatedFiles);
    }

    @Test
    public void runLogsValidationError() {
        var runner = new Runner(options, (file, logFile) -> false, nullMerger);

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
