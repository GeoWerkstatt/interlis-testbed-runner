package ch.geowerkstatt.interlis.testbed.runner;

import java.nio.file.Path;

public record TestOptions(Path basePath, Path ilivalidatorPath) {
    private static final String BASE_DATA_FILENAME = "Successful_Data.xtf";
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
    public Path baseDataFilePath() {
        return basePath.resolve(BASE_DATA_FILENAME);
    }

    /**
     * Gets the path to the output directory.
     *
     * @return the path to the output directory.
     */
    public Path outputPath() {
        return basePath.resolve(OUTPUT_DIR_NAME);
    }
}
