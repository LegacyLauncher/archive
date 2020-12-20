package net.minecraft.launcher.updater;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.connection.ConnectionHelper;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class OfficialVersionList extends RemoteVersionList {
    private static final Logger LOGGER = LogManager.getLogger(OfficialVersionList.class);

    public OfficialVersionList() {
    }

    public RawVersionList getRawList() throws IOException {
        try {
            Object lock = new Object();
            Time.start(lock);
            RawVersionList list;
            try (InputStreamReader reader = getUrl("version_manifest.json")) {
                list = gson.fromJson(reader, RawVersionList.class);
            }
            Iterator var4 = list.versions.iterator();

            while (var4.hasNext()) {
                PartialVersion version = (PartialVersion) var4.next();
                version.setVersionList(this);
            }

            LOGGER.info("Got {} in {}", getClass().getSimpleName(), Time.stop(lock));
            return list;
        } catch(Exception e) {
            if(ConnectionHelper.fixCertException(e, "official-repo") == -1) {
                LOGGER.warn("Official repo is not reachable", e);
                Sentry.capture(new EventBuilder()
                        .withLevel(Event.Level.WARNING)
                        .withMessage("official repo not reachable")
                        .withSentryInterface(new ExceptionInterface(e))
                );
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
