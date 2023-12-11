package ch.geowerkstatt.interlis.testbed.runner.xtf;

import java.nio.file.Path;

public interface XtfMerger {
    /**
     * Merges the patch file into the base file and writes the result to the output file.
     *
     * @param baseFile   the base file
     * @param patchFile  the patch file
     * @param outputFile the output file
     * @return {@code true} if the merge was successful, {@code false} otherwise.
     */
    boolean merge(Path baseFile, Path patchFile, Path outputFile);
}
