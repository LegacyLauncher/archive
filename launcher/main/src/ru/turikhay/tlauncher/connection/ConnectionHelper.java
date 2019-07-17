package ru.turikhay.tlauncher.connection;

import net.minecraft.launcher.updater.OfficialVersionList;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.DataBuilder;

public class ConnectionHelper {
    public static boolean isCertException(Exception e) {
        return e.getMessage().contains("PKIX path building failed") || e.getMessage().contains("the trustAnchors parameter must be non-empty");
    }

    public static int fixCertException(Exception e, String cause, boolean requireRestart) {
        if(!isCertException(e)) {
            return -1;
        }
        if(TLauncher.getInstance().getSettings().isCertFixed()) {
            Sentry.sendError(OfficialVersionList.class, "certificate fix doesn't help", e, DataBuilder.create().add("cause", cause));
            return 0;
        }

        Alert.showError(
                Localizable.get("connection.error.title"),
                (cause == null? "" : Localizable.get("connection.error.ssl.cause." + cause) + "\n\n") +
                        Localizable.get("connection.error.ssl") +
                        (requireRestart? "\n\n" + Localizable.get("connection.error.ssl.restart") : ""),
                null
        );
        TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().sslCheck.setValue(false);
        TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().saveValues();
        Sentry.sendWarning(OfficialVersionList.class, "no certificates", DataBuilder.create("exception", e), DataBuilder.create("cert-fixed", true));
        TLauncher.kill();
        return 1;
    }

    public static int fixCertException(Exception e, String cause) {
        return fixCertException(e, cause, true);
    }
}
