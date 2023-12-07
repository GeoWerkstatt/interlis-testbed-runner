package ch.geowerkstatt.interlis.testbed.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public record TestOptions(Path basePath, Path ilivalidatorPath) {
    private static final String DATA_FILE_EXTENSION = ".xtf";
    private static final String OUTPUT_DIR_NAME = "output";

    /**
     * Creates a new instance of the RunnerOptions class.
     *
     * @param basePath the base path of the testbed.
     */
    public TestOptions {
        basePath = basePath.toAbsolutePath().normalize();
        ilivalidatorPath = ilivalidatorPath.toAbsolutePath().normalize();
    }

    /**
     * Gets the path to the data file that is used as the base for all validations.
     *
     * @return the path to the base data file.
     */
    public Optional<Path> baseDataFilePath() throws IOException {
        try (var dataFiles = findDataFiles(basePath)) {
            return dataFiles.findFirst();
        }
    }

    /**
     * Resolves the path to the output file based on the relative path of the input file.
     *
     * @param filePath    the path to the input file.
     * @param newFileName the name of the output file.
     * @return the path to the output file.
     */
    public Path resolveOutputFilePath(Path filePath, String newFileName) {
        var relativePath = basePath.relativize(filePath.getParent());
        return outputPath().resolve(relativePath).resolve(newFileName);
    }

    /**
     * Gets the path to the output directory.
     *
     * @return the path to the output directory.
     */
    public Path outputPath() {
        return basePath.resolve(OUTPUT_DIR_NAME);
    }

    private static Stream<Path> findDataFiles(Path basePath) throws IOException {
        return Files.find(basePath, 1, (path, attributes) -> path.getFileName().toString().toLowerCase().endsWith(DATA_FILE_EXTENSION));
    }
}
