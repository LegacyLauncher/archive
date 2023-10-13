package net.legacylauncher.ui.converter;

import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.ui.loc.LocalizableStringConverter;

public class LoggerTypeConverter extends LocalizableStringConverter<Configuration.LoggerType> {
    public LoggerTypeConverter() {
        super("settings.logger");
    }

    public Configuration.LoggerType fromString(String from) {
        return Configuration.LoggerType.get(from);
    }

    public String toValue(Configuration.LoggerType from) {
        return from == null ? null : from.toString();
    }

    public String toPath(Configuration.LoggerType from) {
        return from == null ? null : from.toString();
    }

    public Class<Configuration.LoggerType> getObjectClass() {
        return Configuration.LoggerType.class;
    }
}
