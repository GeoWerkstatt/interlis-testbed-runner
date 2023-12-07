package ch.geowerkstatt.interlis.testbed.runner.xtf;

import ch.geowerkstatt.interlis.testbed.runner.TestLogAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    public void validateMergedXtf() throws Exception {
        var baseFile = Path.of(BASE_PATH, "data.xtf");
        var patchFile = Path.of(BASE_PATH, "patch.xtf");
        var outputFile = Path.of(BASE_PATH, "output", "merged.xtf");

        var merger = new XtfFileMerger();

        var mergeResult = merger.merge(baseFile, patchFile, outputFile);

        assertTrue(mergeResult, "Merging should have been successful.");
        assertTrue(Files.exists(outputFile), "Output file should have been created.");

        assertEquals(0, appender.getErrorMessages().size(), "No errors should have been logged.");

        var documentBuilder = merger.createDocumentBuilder();
        var mergedDocument = documentBuilder.parse(outputFile.toFile());
        var baskets = XtfFileMerger.findBaskets(mergedDocument);
        assertTrue(baskets.isPresent(), "Baskets should have been found in merged file.");

        var b1 = baskets.get().get("B1");
        assertNotNull(b1, "Basket B1 should have been found in merged file.");
        assertIterableEquals(List.of("A1", "A2", "A3"), b1.objects().keySet());

        var b2 = baskets.get().get("B2");
        assertNotNull(b2, "Basket B2 should have been found in merged file.");
        assertIterableEquals(List.of("A1"), b2.objects().keySet());
    }
}
