package ch.geowerkstatt.interlis.testbed.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record TestOptions(Path basePath, Path ilivalidatorPath, Path ilivalidatorConfigPath) {
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
        ilivalidatorConfigPath = ilivalidatorConfigPath != null ? ilivalidatorConfigPath.toAbsolutePath().normalize() : null;
    }

    /**
     * Gets the path to the data file that is used as the base for all validations.
     *
     * @return the path to the base data file.
     */
    public Optional<Path> baseDataFilePath() throws IOException {
        try (var dataFiles = findDataFiles(basePath, 1)) {
            return dataFiles.findFirst();
        }
    }

    /**
     * Gets the paths to the patch data files.
     *
     * @return the paths to the patch data files.
     */
    public List<Path> patchDataFiles() throws IOException {
        try (var dataFiles = findDataFiles(basePath, 2)) {
            var outputPath = outputPath();
            return dataFiles
                    .filter(path -> !path.getParent().equals(basePath) && !path.startsWith(outputPath))
                    .toList();
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

    private static Stream<Path> findDataFiles(Path basePath, int maxDepth) throws IOException {
        return Files.find(basePath, maxDepth, TestOptions::isDataFile);
    }

    private static boolean isDataFile(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.getFileName().toString().toLowerCase().endsWith(DATA_FILE_EXTENSION);
    }
}
