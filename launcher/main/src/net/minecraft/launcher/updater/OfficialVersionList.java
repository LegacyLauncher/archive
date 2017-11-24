package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;

public class OfficialVersionList extends RemoteVersionList {
    public OfficialVersionList() {
    }

    public RawVersionList getRawList() throws IOException {
        try {
            Object lock = new Object();
            Time.start(lock);
            RawVersionList list = gson.fromJson(getUrl("version_manifest.json"), RawVersionList.class);
            Iterator var4 = list.versions.iterator();

            while (var4.hasNext()) {
                PartialVersion version = (PartialVersion) var4.next();
                version.setVersionList(this);
            }

            log("Got in", Time.stop(lock), "ms");
            return list;
        } catch(Exception e) {
            if(e.getMessage().contains("PKIX path building failed") || e.getMessage().contains("the trustAnchors parameter must be non-empty")) {
                Alert.showLocError("version.error.title", "version.error.cert", null);
                boolean certFixed = false;
                if(Alert.showLocQuestion("version.error.title", "version.error.cert.question", null)) {
                    certFixed = true;
                    TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.sslCheck.setValue(false);
                    TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.saveValues();
                }
                Sentry.sendWarning(OfficialVersionList.class, "no certificates", DataBuilder.create("exception", e), DataBuilder.create("cert-fixed", certFixed));
                if(certFixed) {
                    TLauncher.kill();
                }
            } else {
                Sentry.sendError(OfficialVersionList.class, "official repo is not reachable", e, null);
            }
            throw new IOException(e);
        }
    }

    @Override
    public boolean hasAllFiles(CompleteVersion var1, OS var2) {
        return true;
    }

    @Override
    protected InputStreamReader getUrl(String var1) throws IOException {
        return new InputStreamReader(new URL("https://launchermeta.mojang.com/mc/game/" + var1).openConnection(U.getProxy()).getInputStream(), FileUtil.DEFAULT_CHARSET);
    }
}
