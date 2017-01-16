package ru.turikhay.tlauncher.repository;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public enum Repository {
    LOCAL_VERSION_REPO,
    OFFICIAL_VERSION_REPO(TLauncher.getOfficialRepo()),
    EXTRA_VERSION_REPO(TLauncher.getExtraRepo()),
    ASSETS_REPO(TLauncher.getAssetsRepo()),
    LIBRARY_REPO(TLauncher.getLibraryRepo()),
    SERVERLIST_REPO(TLauncher.getServerList());

    public static final Repository[] VERSION_REPOS = new Repository[]{LOCAL_VERSION_REPO, OFFICIAL_VERSION_REPO, EXTRA_VERSION_REPO};

    private final AppenderRepoList defaultRepoList;
    private AppenderRepoList repoList;
    private final String lowerName;

    Repository(String[] urlList) {
        defaultRepoList = new AppenderRepoList(name(), U.requireNotNull(urlList, "defaultUrlList"));
        lowerName = name().toLowerCase();
    }

    Repository() {
        this(new String[0]);
    }

    public RepoList getList() {
        return repoList == null? defaultRepoList : repoList;
    }

    public RepoList.RelevantRepoList getRelevant() {
        return getList().getRelevant();
    }

    public InputStream get(String path) throws IOException {
        return getList().read(path);
    }

    public InputStreamReader read(String path) throws IOException {
        return new InputStreamReader(get(path), FileUtil.DEFAULT_CHARSET);
    }

    public boolean isRemote() {
        return getRelevant().getFirst() != null;
    }

    private void update(List<String> repoList) {
        if(repoList == null) {
            U.log("[Repo]", "[" + name() + "]", "repoList is null; won't be updated");
            return;
        }
        this.repoList = new AppenderRepoList(name(), repoList);
    }

    public static void updateList(Map<String, List<String>> repoMap) {
        if(repoMap == null) {
            U.log("[Repo]", "repoMap is null; won't be updated");
            return;
        }

        entryFor: for(Map.Entry<String, List<String>> entry : repoMap.entrySet()) {
            for(Repository repo : values()) {
                if(repo.lowerName.equals(entry.getKey())) {
                    repo.update(entry.getValue());
                    continue entryFor;
                }
            }
        }
    }
}
