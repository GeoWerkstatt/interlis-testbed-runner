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
        var testLogAppender = new TestLogAppender(loggerClass);
        testLogAppender.start();

        var logger = (Logger) LogManager.getLogger(loggerClass);
        logger.get().addAppender(testLogAppender, Level.ALL, null);

        return testLogAppender;
    }

    public void unregister() {
        var logger = (Logger) LogManager.getLogger(loggerClass);
        logger.removeAppender(this);
    }

    public List<LogEntry> getMessages() {
        return messages;
    }

    public List<String> getErrorMessages() {
        return messages.stream()
                .filter(e -> e.level().equals(Level.ERROR))
                .map(LogEntry::message)
                .toList();
    }

    @Override
    public void append(LogEvent event) {
        messages.add(new LogEntry(event.getLevel(), event.getMessage().getFormattedMessage()));
    }
}
