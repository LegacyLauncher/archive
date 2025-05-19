package net.minecraft.launcher.updater;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.connection.ConnectionQueue;
import net.legacylauncher.connection.ConnectionSelector;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.*;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OfficialVersionList extends RemoteVersionList {
    private static final String LAUNCHER_META_PREFIX = "https://launchermeta.mojang.com/mc/game/";

    public OfficialVersionList() {
    }

    @Override
    public RawVersionList getRawList() throws IOException {
        log.info("Requesting official version list");
        URL ogUrl = new URL(LAUNCHER_META_PREFIX + "version_manifest.json");
        List<URL> urlCandidates = new ArrayList<>();
        urlCandidates.add(ogUrl);
        RepositoryProxy.getProxyRepoList().getRelevant().getList()
                .stream()
                .filter(r -> r instanceof RepositoryProxy.ProxyRepo)
                .map(r -> ((RepositoryProxy.ProxyRepo) r).toProxyUrl(ogUrl))
                .forEach(urlCandidates::add);
        ConnectionSelector<EConnection> selector = ConnectionSelector.create(
                new EConnector(),
                10,
                TimeUnit.SECONDS
        );
        long startTime = System.currentTimeMillis();
        ConnectionQueue<EConnection> queue;
        try {
            queue = selector.select(urlCandidates).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        } catch (ExecutionException e) {
            log.warn("Official version list is unreachable");
            throw new IOException(e);
        }
        log.info("Got first connection available");
        while (true) {
            EConnection connection;
            try {
                connection = queue.takeOrThrow();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted", e);
            }
            log.info("Reading: {}", connection.getUrl());
            Response response = connection.getResponse();
            Content content = response.returnContent();
            if (!content.getType().isSameMimeType(ContentType.APPLICATION_JSON)) {
                log.warn("{} returned bad content type: {}", connection.getUrl(), content.getType());
                continue;
            }
            String json = content.asString();
            RawVersionList rawVersionList;
            try {
                rawVersionList = Objects.requireNonNull(gson.fromJson(json, RawVersionList.class));
            } catch (RuntimeException e) {
                log.warn("Couldn't parse official repository response: {}", json, e);
                continue;
            }
            log.info("Got OfficialVersionList in {} ms", System.currentTimeMillis() - startTime);
            try {
                return process(rawVersionList);
            } finally {
                queue.close();
            }
        }
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
