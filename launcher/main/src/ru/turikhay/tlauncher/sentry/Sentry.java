package ru.turikhay.tlauncher.sentry;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;

public final class Sentry {
    private static final Raven raven = RavenFactory.ravenInstance("https://199b63ab5ee943dcad0f00179136cf3f:2e481948222e4d8fbbc6e0e2f33c4ea5@sentry.ely.by/6");

    static Raven getRaven() {
        return raven;
    }

    private Sentry() {
    }
}
