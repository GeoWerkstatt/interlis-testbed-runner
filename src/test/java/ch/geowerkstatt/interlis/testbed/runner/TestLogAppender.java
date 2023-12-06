package ch.geowerkstatt.interlis.testbed.runner;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.ArrayList;
import java.util.List;

/**
 * Appender that can be used to capture log messages from the logger registered to a specific class.
 */
public final class TestLogAppender extends AbstractAppender {
    private final Class<?> loggerClass;
    private final List<LogEntry> messages = new ArrayList<>();

    public record LogEntry(Level level, String message) {
    }

    private TestLogAppender(Class<?> loggerClass) {
        super("TestLogAppender", null, null, true, null);

        this.loggerClass = loggerClass;
    }

    public static TestLogAppender registerAppender(Class<?> loggerClass) {
        var mockedAppender = new TestLogAppender(loggerClass);

        var logger = (Logger) LogManager.getLogger(loggerClass);
        logger.addAppender(mockedAppender);
        logger.setLevel(Level.ALL);

        mockedAppender.start();
        return mockedAppender;
    }

    public void unregister() {
        var logger = (Logger) LogManager.getLogger(loggerClass);
        logger.removeAppender(this);
    }

    public List<LogEntry> getMessages() {
        return messages;
    }

    @Override
    public void append(LogEvent event) {
        messages.add(new LogEntry(event.getLevel(), event.getMessage().getFormattedMessage()));
    }
}
