package ru.turikhay.tlauncher.sentry;

import com.github.zafarkhaja.semver.Version;
import io.sentry.DefaultSentryClientFactory;
import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.context.ContextManager;
import io.sentry.context.SingletonContextManager;
import io.sentry.dsn.Dsn;
import io.sentry.event.User;
import ru.turikhay.util.OS;

import java.util.UUID;

public class SentryConfigurer {
    private static final ContextManager ctxManager = new SingletonContextManager();

    private static SentryClient SENTRY;

    public static void configure(Version version, String shortBrand) {
        SENTRY = Sentry.init("https://6bd0f45848ad4217b1970ae598712dfc@sentry.ely.by/46", new DefaultSentryClientFactory() {
            @Override
            protected ContextManager getContextManager(Dsn dsn) {
                return ctxManager;
            }
        });
        SENTRY.setRelease(version.getNormalVersion());
        SENTRY.setEnvironment(shortBrand);
        SENTRY.setServerName(OS.CURRENT.name());
    }

    public static void setUser(UUID uuid) {
        SENTRY.getContext().setUser(new User(
                uuid.toString(),
                null,
                null,
                null
        ));
    }

    private SentryConfigurer() {
    }
}
