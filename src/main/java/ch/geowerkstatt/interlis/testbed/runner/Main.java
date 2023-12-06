package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.nio.file.Path;

public final class Main {
    private static final String VALIDATOR_PATH_OPTION = "validator";
    private static final String TESTBED_PATH_OPTION = "testbed";

    private Main() {
    }

    /**
     * Program entry point.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage(createCliOptions());
            return;
        }

        var testOptions = parseTestOptions(args);
        var validator = new InterlisValidator(testOptions);
        var runner = new Runner(testOptions, validator);
        runner.run();
    }

    private static TestOptions parseTestOptions(String[] args) {
        var options = createCliOptions();
        try {
            var parser = new DefaultParser();
            var commandLine = parser.parse(options, args);
            return getTestOptions(commandLine);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments: " + e.getMessage());
            printUsage(options);
            System.exit(1);
            return null;
        }
    }

    private static void printUsage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar interlis-testbed-runner.jar [options]", options);
    }

    private static TestOptions getTestOptions(CommandLine commandLine) throws ParseException {
        var basePath = commandLine.hasOption(TESTBED_PATH_OPTION)
                ? Path.of(commandLine.getOptionValue(TESTBED_PATH_OPTION))
                : Path.of(".");
        var validatorPath = Path.of(commandLine.getOptionValue(VALIDATOR_PATH_OPTION));
        return new TestOptions(basePath, validatorPath);
    }

    private static Options createCliOptions() {
        var options = new Options();

        var validatorPathOption = Option.builder("v")
                .argName("path")
                .longOpt(VALIDATOR_PATH_OPTION)
                .hasArg()
                .required()
                .desc("path to ilivalidator.jar")
                .build();
        options.addOption(validatorPathOption);

        var basePathOption = Option.builder("t")
                .argName("path")
                .longOpt(TESTBED_PATH_OPTION)
                .hasArg()
                .desc("base directory of testbed (default: current directory)")
                .build();
        options.addOption(basePathOption);

        return options;
    }
}
