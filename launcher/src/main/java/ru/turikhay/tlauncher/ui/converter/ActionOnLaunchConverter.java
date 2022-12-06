package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ActionOnLaunchConverter extends LocalizableStringConverter<Configuration.ActionOnLaunch> {
    public ActionOnLaunchConverter() {
        super("settings.launch-action");
    }

    public Configuration.ActionOnLaunch fromString(String from) {
        return Configuration.ActionOnLaunch.find(from).orElse(Configuration.ActionOnLaunch.getDefault());
    }

    public String toValue(Configuration.ActionOnLaunch from) {
        return from.toString();
    }

    public String toPath(Configuration.ActionOnLaunch from) {
        return from.toString();
    }

    public Class<Configuration.ActionOnLaunch> getObjectClass() {
        return Configuration.ActionOnLaunch.class;
    }
}
