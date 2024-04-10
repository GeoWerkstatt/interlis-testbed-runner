package ch.geowerkstatt.interlis.testbed.runner.validation;

/**
 * Exception that is thrown when the validation failed to run.
 */
public final class ValidatorException extends Exception {
    /**
     * Creates a new instance of the ValidatorException class.
     *
     * @param message the message of the exception.
     */
    public ValidatorException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of the ValidatorException class.
     *
     * @param message the message of the exception.
     * @param cause   the cause of the exception.
     */
    public ValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of the ValidatorException class.
     *
     * @param cause the cause of the exception.
     */
    public ValidatorException(Throwable cause) {
        super(cause);
    }
}
