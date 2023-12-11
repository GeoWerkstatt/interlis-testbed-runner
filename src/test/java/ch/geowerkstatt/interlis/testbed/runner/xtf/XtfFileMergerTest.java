package ch.geowerkstatt.interlis.testbed.runner.xtf;

import ch.geowerkstatt.interlis.testbed.runner.TestLogAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class XtfFileMergerTest {
    private static final String BASE_PATH = "src/test/data/xtf-merger";

    private TestLogAppender appender;

    @BeforeEach
    public void setup() {
        appender = TestLogAppender.registerAppender(XtfFileMerger.class);
    }

    @AfterEach
    public void teardown() {
        appender.stop();
        appender.unregister();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ili23", "ili24"})
    public void validateMergedXtf(String iliVersion) throws Exception {
        var baseFile = Path.of(BASE_PATH, iliVersion, "data.xtf");
        var patchFile = Path.of(BASE_PATH, iliVersion, "patch.xtf");
        var expectedFile = Path.of(BASE_PATH, iliVersion, "expected.xtf");
        var outputFile = Path.of(BASE_PATH, iliVersion, "output", "merged.xtf");

        var merger = new XtfFileMerger();

        var mergeResult = merger.merge(baseFile, patchFile, outputFile);

        assertTrue(mergeResult, "Merging should have been successful.");
        assertTrue(Files.exists(outputFile), "Output file should have been created.");

        assertEquals(0, appender.getErrorMessages().size(), "No errors should have been logged.");

        assertEqualXtfFiles(expectedFile, outputFile);
    }

    private void assertEqualXtfFiles(Path expectedFile, Path actualFile) throws IOException {
        var expectedXml = Files.readString(expectedFile);
        var actualXml = Files.readString(actualFile);

        var nodeMatcher = new DefaultNodeMatcher(ElementSelectors.byNameAndText);
        var diff = DiffBuilder
                .compare(expectedXml)
                .withTest(actualXml)
                .checkForSimilar()
                .withNodeMatcher(nodeMatcher)
                .ignoreWhitespace()
                .ignoreComments()
                .build();

        for (var difference : diff.getDifferences()) {
            System.out.println(difference);
        }

        assertFalse(diff.hasDifferences(), "Expected and actual XTF files should be equal.");
    }
}
