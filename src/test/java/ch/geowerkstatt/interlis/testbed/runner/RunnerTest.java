package ch.geowerkstatt.interlis.testbed.runner;

import ch.geowerkstatt.interlis.testbed.runner.xtf.XtfMerger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RunnerTest extends MockitoTestBase {
    private static final Path BASE_PATH = Path.of("src/test/data/testbed").toAbsolutePath().normalize();
    private static final Path BASE_PATH_WITH_PATCHES = Path.of("src/test/data/testbed-with-patches").toAbsolutePath().normalize();
    private static final Path ILI_VALIDATOR_PATH = Path.of("ilivalidator.jar");

    private TestLogAppender appender;
    @Mock
    private Validator validatorMock;
    @Mock
    private XtfMerger mergerMock;

    @BeforeEach
    public void setup() {
        appender = TestLogAppender.registerAppender(Runner.class);
    }

    @AfterEach
    public void teardown() {
        appender.stop();
        appender.unregister();
    }

    @Test
    public void runValidatesBaseData() throws IOException, ValidatorException {
        when(validatorMock.validate(any(), any())).thenReturn(true);

        var options = new TestOptions(BASE_PATH, ILI_VALIDATOR_PATH);
        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed without patch files.");

        var errors = appender.getErrorMessages();
        assertIterableEquals(List.of("No patch files found."), errors);

        var expectedBaseDataFile = BASE_PATH.resolve("data.xtf");
        var baseDataFile = options.baseDataFilePath();
        assertFalse(baseDataFile.isEmpty(), "Base data file should have been found.");
        assertEquals(expectedBaseDataFile, baseDataFile.get());

        verify(validatorMock).validate(eq(expectedBaseDataFile), any());
    }

    @Test
    public void runLogsValidationError() throws ValidatorException {
        when(validatorMock.validate(any(), any())).thenReturn(false);

        var options = new TestOptions(BASE_PATH, ILI_VALIDATOR_PATH);
        var runner = new Runner(options, validatorMock, mergerMock);

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

    @Test
    public void runMergesAndValidatesPatchFiles() throws ValidatorException {
        var expectedBaseDataFile = BASE_PATH_WITH_PATCHES.resolve("data.xtf");
        var expectedPatchFile = BASE_PATH_WITH_PATCHES.resolve("constraintA").resolve("testcase-1.xtf");
        var expectedMergedFile = BASE_PATH_WITH_PATCHES.resolve("output").resolve("constraintA").resolve("testcase-1_merged.xtf");

        when(validatorMock.validate(eq(expectedBaseDataFile), any())).thenReturn(true);
        when(validatorMock.validate(eq(expectedMergedFile), any())).thenReturn(false);

        when(mergerMock.merge(any(), any(), any())).thenReturn(true);

        var options = new TestOptions(BASE_PATH_WITH_PATCHES, ILI_VALIDATOR_PATH);

        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertTrue(runResult, "Testbed run should have succeeded.");

        var errors = appender.getErrorMessages();
        assertTrue(errors.isEmpty(), "No errors should have been logged.");

        verify(validatorMock).validate(eq(expectedBaseDataFile), any());
        verify(validatorMock).validate(eq(expectedMergedFile), any());

        verify(mergerMock).merge(eq(expectedBaseDataFile), eq(expectedPatchFile), eq(expectedMergedFile));
    }
}
