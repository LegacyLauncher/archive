package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.event.BreadcrumbBuilder;
import com.getsentry.raven.event.Event;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.U;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Breadcrumb {

    private final Event.Level level;
    private final String clazz;
    private final String message;
    private final Map<String, String> data = new LinkedHashMap<String, String>();

    public static Breadcrumb info(Class clazz, String message) {
        return new Breadcrumb(Event.Level.INFO, clazz, message);
    }

    private Breadcrumb(Event.Level level, Class clazz, String message) {
        this.level = level;
        this.clazz = clazz == null? "unknown" : clazz.getSimpleName();
        this.message = message;
    }

    public Breadcrumb data(String key, Object value) {
        data.put(key, U.toLog(value));
        return this;
    }

    public void push() {
        Sentry.getRaven().getContext().recordBreadcrumb(
                new BreadcrumbBuilder()
                        .setLevel(level.name())
                        .setCategory("class:" + clazz)
                        .setMessage(message)
                        .setData(data.isEmpty()? null : data)
                        .build()
        );
        U.log("[Breadcrumb]", this);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("level", level)
                .append("class", clazz)
                .append("message", message)
                .append("data", data)
                .build();
    }
}
