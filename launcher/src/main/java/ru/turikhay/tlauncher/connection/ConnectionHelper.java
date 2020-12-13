package ru.turikhay.tlauncher.connection;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;

public class ConnectionHelper {
    public static boolean isCertException(Throwable e) {
        return e.getMessage().contains("PKIX path building failed") || e.getMessage().contains("the trustAnchors parameter must be non-empty");
    }

    public static int fixCertException(Throwable e, String cause, boolean requireRestart) {
        if(!isCertException(e)) {
            return -1;
        }
        if(TLauncher.getInstance().getSettings().isCertFixed()) {
            Sentry.capture(new EventBuilder()
                    .withLevel(Event.Level.ERROR)
                    .withMessage("cert exception with cert fix")
                    .withSentryInterface(new ExceptionInterface(e))
                    .withExtra("cause", cause)
            );
            return 0;
        }
        Sentry.capture(new EventBuilder()
                .withLevel(Event.Level.INFO)
                .withMessage("cert fix")
        );
        Alert.showError(
                Localizable.get("connection.error.title"),
                (cause == null? "" : Localizable.get("connection.error.ssl.cause." + cause) + "\n\n") +
                        Localizable.get("connection.error.ssl") +
                        (requireRestart? "\n\n" + Localizable.get("connection.error.ssl.restart") : ""),
                null
        );
        TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().sslCheck.setValue(false);
        TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().saveValues();
        TLauncher.kill();
        return 1;
    }

    public static int fixCertException(Exception e, String cause) {
        return fixCertException(e, cause, true);
    }
}
