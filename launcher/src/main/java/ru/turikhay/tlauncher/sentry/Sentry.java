package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import ru.turikhay.util.DataBuilder;

public final class Sentry {
    private static final Raven raven = RavenFactory.ravenInstance("https://199b63ab5ee943dcad0f00179136cf3f:2e481948222e4d8fbbc6e0e2f33c4ea5@sentry.ely.by/6");

    static Raven getRaven() {
        return raven;
    }

    public static void sendError(Class clazz, String message, Throwable t, DataBuilder data, DataBuilder tags) {
        SentryContext.GLOBAL_CONTEXT.sendError(clazz, message, t, data, tags);
    }

    public static void sendError(Class clazz, String message, Throwable t, DataBuilder data) {
        sendError(clazz, message, t, data, null);
    }

    public static void sendWarning(Class clazz, String message, DataBuilder data, DataBuilder tags) {
        SentryContext.GLOBAL_CONTEXT.sendWarning(clazz, message, data, tags);
    }

    public static void sendWarning(Class clazz, String message, DataBuilder data) {
        sendWarning(clazz, message, data, null);
    }

    private Sentry() {
    }
}