package ru.turikhay.tlauncher.logger;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

@Plugin(name = "SwingLogger", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class SwingLoggerAppender extends AbstractAppender {
    private LoggerInterface li;

    protected SwingLoggerAppender(String name,
                                  Filter filter,
                                  Layout<? extends Serializable> layout,
                                  boolean ignoreExceptions,
                                  Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.li = new LoggerBuffer();
    }

    public LoggerInterface getLoggerInterface() {
        return li;
    }

    public void setLoggerInterface(LoggerInterface li) {
        this.li = li;
    }

    @Override
    public void append(LogEvent event) {
        String message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        li.print(message);
    }

    @PluginFactory
    public static SwingLoggerAppender createAppender(@PluginAttribute("name") String name,
                                                     @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                                     @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                     @PluginElement("Filters") Filter filter) {

        if (name == null) {
            LOGGER.error("No name provided for StubAppender");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new SwingLoggerAppender(name, filter, layout, ignoreExceptions, null);
    }
}
