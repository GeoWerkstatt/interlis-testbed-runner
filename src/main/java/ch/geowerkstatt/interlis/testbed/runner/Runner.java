package ch.geowerkstatt.interlis.testbed.runner;

import ch.geowerkstatt.interlis.testbed.runner.xtf.XtfMerger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class Runner {
    private static final Logger LOGGER = LogManager.getLogger();

    private final TestOptions options;
    private final Validator validator;
    private final XtfMerger xtfMerger;
    private Path baseFilePath;

    /**
     * Creates a new instance of the Runner class.
     *
     * @param options   the test options.
     * @param validator the validator to use.
     * @param xtfMerger the XTF merger to use.
     */
    public Runner(TestOptions options, Validator validator, XtfMerger xtfMerger) {
        this.options = options;
        this.validator = validator;
        this.xtfMerger = xtfMerger;
    }

    /**
     * Runs the testbed validation.
     *
     * @return {@code true} if the validation was successful, {@code false} otherwise.
     */
    public boolean run() {
        LOGGER.info("Starting validation of testbed at " + options.basePath());

        try {
            if (!validateBaseData()) {
                return false;
            }

            if (!mergeAndValidateTransferFiles()) {
                return false;
            }

            LOGGER.info("Validation of testbed completed.");
            return true;
        } catch (ValidatorException e) {
            LOGGER.error("Validation could not run, check the configuration.", e);
            return false;
        }
    }

    private boolean validateBaseData() throws ValidatorException {
        try {
            var baseFilePath = options.baseDataFilePath();
            if (baseFilePath.isEmpty()) {
                LOGGER.error("No base data file found.");
                return false;
            }
            this.baseFilePath = baseFilePath.get();
        } catch (IOException e) {
            throw new ValidatorException(e);
        }

        LOGGER.info("Validating base data file " + baseFilePath);
        var filenameWithoutExtension = StringUtils.getFilenameWithoutExtension(baseFilePath.getFileName().toString());
        var logFile = options.resolveOutputFilePath(baseFilePath, filenameWithoutExtension + ".log");

        var valid = validator.validate(baseFilePath, logFile);
        if (valid) {
            LOGGER.info("Validation of " + baseFilePath + " completed successfully.");
        } else {
            LOGGER.error("Validation of " + baseFilePath + " failed. See " + logFile + " for details.");
        }
        return valid;
    }

    private boolean mergeAndValidateTransferFiles() throws ValidatorException {
        List<Path> patchFiles;
        try {
            patchFiles = options.patchDataFiles();
        } catch (IOException e) {
            throw new ValidatorException(e);
        }

        if (patchFiles.isEmpty()) {
            LOGGER.error("No patch files found.");
            return false;
        }

        var valid = true;
        for (var patchFile : patchFiles) {
            var patchFileNameWithoutExtension = StringUtils.getFilenameWithoutExtension(patchFile.getFileName().toString());
            var mergedFile = options.resolveOutputFilePath(patchFile, patchFileNameWithoutExtension + "_merged.xtf");
            if (!xtfMerger.merge(baseFilePath, patchFile, mergedFile)) {
                valid = false;
                continue;
            }

            var logFile = mergedFile.getParent().resolve(patchFileNameWithoutExtension + ".log");
            var mergedFileValid = validator.validate(mergedFile, logFile);
            if (mergedFileValid) {
                LOGGER.error("Validation of " + mergedFile + " was expected to fail but completed successfully.");
                valid = false;
            }
        }

        return valid;
    }
}
