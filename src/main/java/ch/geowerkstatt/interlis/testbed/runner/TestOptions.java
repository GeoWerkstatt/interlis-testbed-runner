package ch.geowerkstatt.interlis.testbed.runner;

import java.nio.file.Path;

public record TestOptions(Path basePath, Path ilivalidatorPath) {
    /**
     * Creates a new instance of the RunnerOptions class.
     *
     * @param basePath the base path of the testbed.
     */
    public TestOptions {
        basePath = basePath.toAbsolutePath().normalize();
        ilivalidatorPath = ilivalidatorPath.toAbsolutePath().normalize();
    }
}
