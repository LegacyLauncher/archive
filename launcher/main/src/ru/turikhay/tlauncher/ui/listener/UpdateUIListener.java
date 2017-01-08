package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.UpdateListener;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

public class UpdateUIListener implements UpdateListener {
    private final TLauncher t;
    private final Update u;

    public UpdateUIListener(Update u) {
        if (u == null) {
            throw new NullPointerException();
        } else {
            t = TLauncher.getInstance();
            this.u = u;
            u.addListener(this);
        }
    }

    public void push() {
        block();
        u.download(true);
    }

    public void onUpdateError(Update u, Throwable e) {
        if (Alert.showLocQuestion("updater.error.title", "updater.download-error", e)) {
            URLConnection connection;
            try {
                connection = Repository.EXTRA_VERSION_REPO.getRelevant().getFirst().get(u.getLink().substring(1), U.getReadTimeout(), U.getProxy());
            } catch (IOException ioE) {
                U.log(ioE);
                return;
            }
            openUpdateLink(connection.getURL().toString());
        }

        unblock();
    }

    public void onUpdateDownloading(Update u) {
    }

    public void onUpdateDownloadError(Update u, Throwable e) {
        onUpdateError(u, e);
    }

    public void onUpdateReady(Update u) {
        onUpdateReady(u, false);
    }

    private static void onUpdateReady(Update u, boolean showChangeLog) {
        Alert.showLocWarning("updater.downloaded", showChangeLog ? u.getDescription() : null);
        u.apply();
    }

    public void onUpdateApplying(Update u) {
    }

    public void onUpdateApplyError(Update u, Throwable e) {
        if (Alert.showLocQuestion("updater.save-error", e)) {
            URLConnection connection;
            try {
                connection = Repository.EXTRA_VERSION_REPO.getRelevant().getFirst().get(u.getLink().substring(1), U.getReadTimeout(), U.getProxy());
            } catch (IOException ioE) {
                U.log(ioE);
                return;
            }
            openUpdateLink(connection.getURL().toString());
        }

        unblock();
    }

    private static boolean openUpdateLink(String link) {
        try {
            if (OS.openLink(new URI(link), false)) {
                return true;
            }
        } catch (URISyntaxException var2) {
        }

        Alert.showLocError("updater.found.cannotopen", link);
        return false;
    }

    private void block() {
        Blocker.block(t.getFrame().mp, "updater");
    }

    private void unblock() {
        Blocker.unblock(t.getFrame().mp, "updater");
    }
}
