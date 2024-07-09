package net.minecraft.launcher.updater;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.EHttpClient;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.Time;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import org.apache.commons.io.IOExceptionList;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class OfficialVersionList extends RemoteVersionList {
    private static final String LAUNCHER_META_PREFIX = "https://launchermeta.mojang.com/mc/game/";

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
                log.debug("Fetching official repository: {}", url);
                String content;
                try {
                    content = EHttpClient.toString(Request.get(url));
                } catch (IOException ioE) {
                    log.warn("Official repository is not available: {}", url, ioE);
                    ioEList.add(ioE);
                    continue;
                }
                RawVersionList rawVersionList;
                try {
                    rawVersionList = Objects.requireNonNull(gson.fromJson(content, RawVersionList.class));
                } catch (RuntimeException e) {
                    log.warn("Couldn't parse official repository response: {}", content, e);
                    ioEList.add(new IOException("invalid json", e));
                    continue;
                }
                log.info("Got OfficialVersionList in {} ms", Time.stop(currentUrlLock));
                return process(rawVersionList);
            } finally {
                Time.stop(currentUrlLock);
            }
        }
        log.warn("Official repository is not reachable");
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

    @Override
    protected InputStreamReader getUrl(String var1) throws IOException {
        return Repository.PROXIFIED_REPO.read(LAUNCHER_META_PREFIX + var1);
    }
}
