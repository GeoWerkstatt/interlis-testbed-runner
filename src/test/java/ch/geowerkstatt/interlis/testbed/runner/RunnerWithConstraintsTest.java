package ch.geowerkstatt.interlis.testbed.runner;

import ch.geowerkstatt.interlis.testbed.runner.validation.Validator;
import ch.geowerkstatt.interlis.testbed.runner.validation.ValidatorException;
import ch.geowerkstatt.interlis.testbed.runner.xtf.XtfMerger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RunnerWithConstraintsTest extends MockitoTestBase {
    private static final Path BASE_PATH = Path.of("src/test/data/testbed-with-patches").toAbsolutePath().normalize();
    private static final String CONSTRAINT_NAME = "constraintA";
    private static final Path BASE_DATA_FILE = BASE_PATH.resolve("data.xtf");
    private static final Path PATCH_FILE = BASE_PATH.resolve(CONSTRAINT_NAME).resolve("testcase-1.xtf");
    private static final Path MERGED_FILE = BASE_PATH.resolve("output").resolve(CONSTRAINT_NAME).resolve("testcase-1_merged.xtf");
    private static final Path MERGED_LOG_FILE = BASE_PATH.resolve("output").resolve(CONSTRAINT_NAME).resolve("testcase-1.log");

    private TestOptions options;
    private TestLogAppender appender;
    @Mock
    private Validator validatorMock;
    @Mock
    private XtfMerger mergerMock;

    @BeforeEach
    public void setup() {
        options = new TestOptions(BASE_PATH, Path.of("ilivalidator.jar"), Optional.empty());
        appender = TestLogAppender.registerAppender(Runner.class);

        when(mergerMock.merge(any(), any(), any())).thenReturn(true);
    }

    @AfterEach
    public void teardown() {
        appender.stop();
        appender.unregister();
    }

    @Test
    public void runMergesAndValidatesPatchFiles() throws ValidatorException {
        when(validatorMock.validate(eq(BASE_DATA_FILE), any())).thenReturn(true);
        when(validatorMock.validate(MERGED_FILE, MERGED_LOG_FILE)).thenReturn(false);
        when(validatorMock.containsConstraintError(MERGED_LOG_FILE, CONSTRAINT_NAME)).thenReturn(true);

        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertTrue(runResult, "Testbed run should have succeeded.");

        var errors = appender.getErrorMessages();
        assertTrue(errors.isEmpty(), "No errors should have been logged.");

        verify(validatorMock).validate(eq(BASE_DATA_FILE), any());
        verify(validatorMock).validate(eq(MERGED_FILE), eq(MERGED_LOG_FILE));

        verify(mergerMock).merge(eq(BASE_DATA_FILE), eq(PATCH_FILE), eq(MERGED_FILE));
    }

    @Test
    public void runFailsIfMergeFails() throws ValidatorException {
        when(validatorMock.validate(eq(BASE_DATA_FILE), any())).thenReturn(true);
        when(mergerMock.merge(eq(BASE_DATA_FILE), eq(PATCH_FILE), any())).thenReturn(false);

        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed if file merging failed.");

        verify(validatorMock).validate(eq(BASE_DATA_FILE), any());
        verify(validatorMock, never()).validate(eq(MERGED_FILE), eq(MERGED_LOG_FILE));

        verify(mergerMock).merge(eq(BASE_DATA_FILE), eq(PATCH_FILE), eq(MERGED_FILE));
    }

    @Test
    public void runFailsIfMergedFileIsValid() throws ValidatorException {
        when(validatorMock.validate(eq(BASE_DATA_FILE), any())).thenReturn(true);
        when(validatorMock.validate(MERGED_FILE, MERGED_LOG_FILE)).thenReturn(true);

        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed if merged file is valid.");

        var errors = appender.getErrorMessages();
        assertEquals(1, errors.size(), "One error should have been logged.");
        var errorMessage = errors.getFirst();
        assertTrue(
                Pattern.matches("Validation of .*? was expected to fail but completed successfully\\.", errorMessage),
                "Expected: Validation of <merged xtf file> was expected to fail but completed successfully. Actual: '" + errorMessage + "'.");

        verify(validatorMock).validate(eq(BASE_DATA_FILE), any());
        verify(validatorMock).validate(eq(MERGED_FILE), any());

        verify(mergerMock).merge(eq(BASE_DATA_FILE), eq(PATCH_FILE), eq(MERGED_FILE));
    }

    @Test
    public void runFailsIfNoConstraintErrorInLog() throws ValidatorException {
        when(validatorMock.validate(eq(BASE_DATA_FILE), any())).thenReturn(true);
        when(validatorMock.validate(MERGED_FILE, MERGED_LOG_FILE)).thenReturn(false);
        when(validatorMock.containsConstraintError(MERGED_LOG_FILE, CONSTRAINT_NAME)).thenReturn(false);

        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed if log file does not contain a constraint error.");

        var errors = appender.getErrorMessages();
        assertEquals(1, errors.size(), "One error should have been logged.");
        var errorMessage = errors.getFirst();
        var expectedErrorMessageStart = "Could not verify constraint " + CONSTRAINT_NAME + " for merged file";
        assertTrue(
                errorMessage.startsWith(expectedErrorMessageStart),
                "Expected error to start with: " + expectedErrorMessageStart + ". Actual: '" + errorMessage + "'.");

        verify(validatorMock).validate(eq(BASE_DATA_FILE), any());
        verify(validatorMock).validate(eq(MERGED_FILE), eq(MERGED_LOG_FILE));
        verify(validatorMock).containsConstraintError(eq(MERGED_LOG_FILE), eq(CONSTRAINT_NAME));

        verify(mergerMock).merge(eq(BASE_DATA_FILE), eq(PATCH_FILE), eq(MERGED_FILE));
    }
}
