package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.LogManager;

public final class Main {
    private Main() {
    }

    /**
     * Program entry point.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        var logger = LogManager.getLogger();
        logger.info("Hello World!");
    }
}
