package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.interfaces.ExceptionInterface;
import com.getsentry.raven.event.interfaces.UserInterface;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.DataBuilder;
import ru.turikhay.util.OS;
import ru.turikhay.util.windows.WMIProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SentryContext {
    static final SentryContext GLOBAL_CONTEXT = new SentryContext(null, "global");

    private final SentryContext parent;
    private final String name;
    private final List<Breadcrumb> breadcrumbList = new ArrayList<Breadcrumb>();

    private SentryContext(SentryContext parentContext, String name) {
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
                .withServerName(OS.NAME)
                .withPlatform(OS.JAVA_VERSION.getVersion())
                .withRelease(String.valueOf(TLauncher.getVersion()));

        b.withTag("brand", TLauncher.getBrand());
        addAv(b);

        if(TLauncher.getInstance() != null) {
            TLauncher tl = TLauncher.getInstance();
            if(tl.getSettings() != null) {
                b.withSentryInterface(new UserInterface(tl.getSettings().getClient().toString(), null, null, null));
            }
            if(tl.getLang() != null) {
                b.withTag("locale", tl.getLang() == null ? null : String.valueOf(tl.getLang().getLocale()));
            }
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

    private void addAv(EventBuilder b) {
        if(!OS.WINDOWS.isCurrent()) {
            return;
        }

        List<String> avList = WMIProvider.getAvSoftwareList();
        int count = 0;

        for(String av : avList) {
            if("Windows Defender".equals(av)) {
                continue;
            }

            if(count > 1) {
                b.withTag("av" + String.valueOf(count), av);
            } else {
                b.withTag("av", av);
                count++;
            }
        }
    }

    public static SentryContext createWithName(String name) {
        if(GLOBAL_CONTEXT.getName().equals(name)) {
            throw new IllegalArgumentException(GLOBAL_CONTEXT.getName() + "cannot be created twice");
        }
        return new SentryContext(GLOBAL_CONTEXT, name);
    }
}
