package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.BreadcrumbBuilder;
import com.getsentry.raven.event.Event;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.DataBuilder;
import ru.turikhay.util.U;

public final class SentryBreadcrumb {
    private final Event.Level level;
    private final String clazz;
    private final String message;
    private final DataBuilder dataBuilder = new DataBuilder();

    public static SentryBreadcrumb info(Class clazz, String message) {
        return new SentryBreadcrumb(Event.Level.INFO, clazz, message);
    }

    private SentryBreadcrumb(Event.Level level, Class clazz, String message) {
        this.level = level;
        this.clazz = clazz == null? null : clazz.getSimpleName();
        this.message = message;
    }

    public SentryBreadcrumb data(String key, Object value) {
        dataBuilder.add(key, value);
        return this;
    }

    public SentryBreadcrumb data(DataBuilder builder) {
        dataBuilder.add(builder);
        return this;
    }

    public void push() {
        push(SentryContext.GLOBAL_CONTEXT);
    }

    public void push(SentryContext context) {
        U.requireNotNull(context, "context");

        if(context == SentryContext.GLOBAL_CONTEXT) {
            U.log("[Breadcrumb]", this);
            context = SentryContext.GLOBAL_CONTEXT;
        } else {
            U.log("[Breadcrumb:"+ context.getName() +"]", this);
        }
        context.record(build(context));
    }

    private Breadcrumb build(SentryContext context) {
        return new BreadcrumbBuilder()
                .setLevel(level.name())
                .setCategory(context.getName())
                .setMessage(clazz == null? message : clazz + ":" + message)
                .setData(dataBuilder.build())
                .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("level", level)
                .append("class", clazz)
                .append("message", message)
                .append("data", dataBuilder)
                .build();
    }
}
