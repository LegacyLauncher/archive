package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.DataBuilder;
import ru.turikhay.util.OS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SentryBreadcrumbContext {
    static final SentryBreadcrumbContext GLOBAL_CONTEXT = new SentryBreadcrumbContext(null, "global");

    private final SentryBreadcrumbContext parent;
    private final String name;
    private final List<Breadcrumb> breadcrumbList = new ArrayList<Breadcrumb>();

    SentryBreadcrumbContext(SentryBreadcrumbContext parentContext, String name) {
        this.parent = parentContext;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void record(Breadcrumb breadcrumb) {
        breadcrumbList.add(breadcrumb);
    }

    public void sendError(Class clazz, String message, Throwable t, DataBuilder data) {
        Sentry.getRaven().sendEvent(
                buildEvent(Event.Level.ERROR, clazz, message, data)
                        .withSentryInterface(new ExceptionInterface(t))
                        .build()
        );
    }

    public void sendWarning(Class clazz, String message, DataBuilder data) {
        Sentry.getRaven().sendEvent(
                buildEvent(Event.Level.WARNING, clazz, message, data)
                .build()
        );
    }

    private EventBuilder buildEvent(Event.Level level, Class clazz, String message, DataBuilder data) {
        EventBuilder b = new EventBuilder()
                .withLevel(level)
                .withCulprit(clazz == null ? null : clazz.toString())
                .withMessage(message == null ? "(null message)" : message)
                .withPlatform(OS.NAME)
                .withEnvironment(OS.JAVA_VERSION.getVersion())
                .withRelease(String.valueOf(TLauncher.getVersion()));

        b.withTag("brand", TLauncher.getBrand());

        if(TLauncher.getInstance() != null) {
            TLauncher tl = TLauncher.getInstance();
            b.withTag("locale", tl.getLang() == null? null : String.valueOf(tl.getLang().getLocale()));
        }

        if (data != null) {
            for (Map.Entry<String, String> entry : data.build().entrySet()) {
                b.withExtra(entry.getKey(), entry.getValue());
            }
        }

        List<Breadcrumb> finalBreadcrumbList = new ArrayList<Breadcrumb>();
        if (parent != null) {
            finalBreadcrumbList.addAll(parent.breadcrumbList);
        }
        finalBreadcrumbList.addAll(breadcrumbList);
        b.withBreadcrumbs(finalBreadcrumbList);

        return b;
    }

    public static SentryBreadcrumbContext createWithName(String name) {
        return new SentryBreadcrumbContext(GLOBAL_CONTEXT, name);
    }
}
