package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.connection.ConnectionHelper;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
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
            if(ConnectionHelper.fixCertException(e, "official-repo") == -1) {
                Sentry.sendError(OfficialVersionList.class, "official repo is not reachable", e, null);
            }
            if(e instanceof IOException) {
                throw e;
            } else {
                throw new IOException(e);
            }
        }
    }

    @Override
    public boolean hasAllFiles(CompleteVersion var1, OS var2) {
        return true;
    }

    private static final String LAUNCHER_META_PREFIX = "https://launchermeta.mojang.com/mc/game/";
    @Override
    protected InputStreamReader getUrl(String var1) throws IOException {
        return Repository.PROXIFIED_REPO.read(LAUNCHER_META_PREFIX + var1);
    }
}
