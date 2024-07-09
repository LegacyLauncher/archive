package net.legacylauncher.repository;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.configuration.Static;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public enum Repository {
    LOCAL_VERSION_REPO,
    OFFICIAL_VERSION_REPO(Static.getOfficialRepo()),
    EXTRA_VERSION_REPO(Static.getExtraRepo()),
    ASSETS_REPO(Static.getAssetsRepo()),
    LIBRARY_REPO(Static.getLibraryRepo()),
    SERVERLIST_REPO(Static.getServerList()),
    PROXIFIED_REPO(RepositoryProxy.getProxyRepoList());

    public static final Repository[] VERSION_REPOS = new Repository[]{LOCAL_VERSION_REPO, OFFICIAL_VERSION_REPO, EXTRA_VERSION_REPO};

    private final RepoList defaultRepoList;
    private RepoList repoList;
    private final String lowerName = name().toLowerCase(java.util.Locale.ROOT);

    Repository(List<String> urlList) {
        defaultRepoList = new AppenderRepoList(name(), Objects.requireNonNull(urlList, "defaultUrlList"));
    }

    Repository(RepoList repoList) {
        defaultRepoList = Objects.requireNonNull(repoList, "repoList");
    }

    Repository() {
        this(Collections.emptyList());
    }

    public RepoList getList() {
        return repoList == null ? defaultRepoList : repoList;
    }

    public RepoList.RelevantRepoList getRelevant() {
        return getList().getRelevant();
    }

    public InputStream get(String path) throws IOException {
        return getList().read(path);
    }

    public InputStreamReader read(String path) throws IOException {
        return new InputStreamReader(get(path), StandardCharsets.UTF_8);
    }

    public boolean isRemote() {
        return getRelevant().getFirst() != null;
    }

    private void update(List<String> repoList) {
        if (repoList == null) {
            log.debug("repoList passed to is null; {} update discarded", name());
            return;
        }
        this.repoList = new AppenderRepoList(name(), repoList);
    }

    public static void updateList(Map<String, List<String>> repoMap) {
        if (repoMap == null) {
            log.debug("repoMap is null; update discarded");
            return;
        }

        for (Map.Entry<String, List<String>> entry : repoMap.entrySet()) {
            for (Repository repo : values()) {
                if (repo.lowerName.equals(entry.getKey())) {
                    repo.update(entry.getValue());
                    break;
                }
            }
        }
    }
}
