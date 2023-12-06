package ch.geowerkstatt.interlis.testbed.runner;

public final class StringUtils {
    private StringUtils() {
    }

    /**
     * Gets the filename without the extension.
     *
     * @param filename the filename to get the name from.
     * @return the filename without the extension.
     */
    public static String getFilenameWithoutExtension(String filename) {
        var lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return filename;
        }

        return filename.substring(0, lastDotIndex);
    }
}
