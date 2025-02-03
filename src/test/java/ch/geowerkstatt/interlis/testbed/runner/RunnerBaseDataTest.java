package ch.geowerkstatt.interlis.testbed.runner;

import ch.geowerkstatt.interlis.testbed.runner.validation.Validator;
import ch.geowerkstatt.interlis.testbed.runner.validation.ValidatorException;
import ch.geowerkstatt.interlis.testbed.runner.xtf.XtfMerger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RunnerBaseDataTest extends MockitoTestBase {
    private static final Path BASE_PATH = Path.of("src/test/data/testbed").toAbsolutePath().normalize();
    private static final Path BASE_DATA_FILE = BASE_PATH.resolve("data.xtf");

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
    }

    @AfterEach
    public void teardown() {
        appender.stop();
        appender.unregister();
    }

    @Test
    public void runValidatesBaseData() throws IOException, ValidatorException {
        when(validatorMock.validate(any(), any())).thenReturn(true);

        var runner = new Runner(options, validatorMock, mergerMock);

        var runResult = runner.run();

        assertFalse(runResult, "Testbed run should have failed without patch files.");

        var errors = appender.getErrorMessages();
        assertIterableEquals(List.of("No patch files found."), errors);

        var baseDataFile = options.baseDataFilePath();
        assertFalse(baseDataFile.isEmpty(), "Base data file should have been found.");
        assertEquals(BASE_DATA_FILE, baseDataFile.get());

        verify(validatorMock).validate(eq(BASE_DATA_FILE), any());
    }

    @Test
    public void runLogsValidationError() throws ValidatorException {
        when(validatorMock.validate(any(), any())).thenReturn(false);

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

        verify(validatorMock).validate(eq(BASE_DATA_FILE), any());
    }
}
