package net.minecraft.launcher.updater;

import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import org.apache.commons.io.IOExceptionList;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.util.EHttpClient;
import ru.turikhay.util.OS;
import ru.turikhay.util.Time;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OfficialVersionList extends RemoteVersionList {
    private static final Logger LOGGER = LogManager.getLogger(OfficialVersionList.class);

    public OfficialVersionList() {
    }

    public RawVersionList getRawList() throws IOException {
        URL ogUrl = new URL(LAUNCHER_META_PREFIX + "version_manifest.json");
        List<String> urlCandidates = new ArrayList<>();
        urlCandidates.add(ogUrl.toString());
        RepositoryProxy.getProxyRepoList().getRelevant().getList()
                .stream()
                .filter(r -> r instanceof RepositoryProxy.ProxyRepo)
                .map(r -> ((RepositoryProxy.ProxyRepo) r).prefixUrl(ogUrl))
                .forEach(urlCandidates::add);
        List<IOException> ioEList = new ArrayList<>(urlCandidates.size());
        for (String url : urlCandidates) {
            Object currentUrlLock = new Object();
            Time.start(currentUrlLock);
            try {
                LOGGER.debug("Fetching official repository: {}", url);
                String content;
                try {
                    content = EHttpClient.toString(Request.Get(url));
                } catch (IOException ioE) {
                    LOGGER.warn("Official repository is not available: {}", url, ioE);
                    ioEList.add(ioE);
                    continue;
                }
                RawVersionList rawVersionList;
                try {
                    rawVersionList = Objects.requireNonNull(gson.fromJson(content, RawVersionList.class));
                } catch (RuntimeException e) {
                    LOGGER.warn("Couldn't parse official repository response: {}", content, e);
                    ioEList.add(new IOException("invalid json", e));
                    continue;
                }
                LOGGER.info("Got OfficialVersionList in {} ms", Time.stop(currentUrlLock));
                return process(rawVersionList);
            } finally {
                Time.stop(currentUrlLock);
            }
        }
        LOGGER.warn("Official repository is not reachable");
        throw new IOExceptionList(ioEList);
    }

    private RawVersionList process(RawVersionList list) {
        for (PartialVersion version : list.versions) {
            version.setVersionList(this);
        }
        return list;
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
