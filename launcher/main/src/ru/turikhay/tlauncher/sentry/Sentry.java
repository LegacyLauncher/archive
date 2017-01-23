package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import ru.turikhay.util.DataBuilder;

public final class Sentry {
    private static final Raven raven = RavenFactory.ravenInstance("https://199b63ab5ee943dcad0f00179136cf3f:2e481948222e4d8fbbc6e0e2f33c4ea5@sentry.ely.by/6");

    static Raven getRaven() {
        return raven;
    }

    public static void sendError(Class clazz, String message, Throwable t, DataBuilder data) {
        SentryContext.GLOBAL_CONTEXT.sendError(clazz, message, t, data);
    }

    private Sentry() {
    }
}
