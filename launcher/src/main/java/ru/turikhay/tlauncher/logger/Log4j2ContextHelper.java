package ru.turikhay.tlauncher.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;

import java.io.File;

public final class Log4j2ContextHelper {

    public static LoggerContext getContext() {
        return (LoggerContext) LogManager.getContext(false);
    }

    public static <A extends Appender> A getAppender(String name, Class<A> appenderClass) {
        Appender appender = getContext().getConfiguration().getAppender(name);
        if (appender == null) {
            throw new RuntimeException(String.format(java.util.Locale.ROOT,
                    "couldn't find appender %s of class %s", name, appenderClass.getSimpleName()
            ));
        }
        if (!appenderClass.isInstance(appender)) {
            throw new RuntimeException(String.format(java.util.Locale.ROOT,
                    "expected appender %s to be %s, but got %s",
                    name, appenderClass.getSimpleName(), appender.getClass().getSimpleName()
            ));
        }
        return appenderClass.cast(appender);
    }

    public static SwingLoggerAppender getSwingLoggerAppender() {
        return getAppender("swingLogger", SwingLoggerAppender.class);
    }

    public static LogFile getCurrentLogFile() {
        RollingFileAppender fileAppender = getAppender("file", RollingFileAppender.class);
        return LogFile.usingUTF8(new File(fileAppender.getFileName()));
    }

    private Log4j2ContextHelper() {
    }
}
