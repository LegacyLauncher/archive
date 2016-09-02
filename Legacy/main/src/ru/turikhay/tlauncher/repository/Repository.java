package ru.turikhay.tlauncher.repository;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public enum Repository {
    LOCAL_VERSION_REPO,
    OFFICIAL_VERSION_REPO(TLauncher.getOfficialRepo()),
    EXTRA_VERSION_REPO(TLauncher.getExtraRepo()),
    ASSETS_REPO(TLauncher.getAssetsRepo()),
    LIBRARY_REPO(TLauncher.getLibraryRepo()),
    SERVERLIST_REPO(TLauncher.getServerList());

    public static final Repository[] VERSION_REPOS = new Repository[]{LOCAL_VERSION_REPO, OFFICIAL_VERSION_REPO, EXTRA_VERSION_REPO};

    private final AppenderRepoList repoList;
    private final String lowerName;

    Repository(String[] urlList) {
        repoList = new AppenderRepoList(name(), U.requireNotNull(urlList, "urlList"));
        lowerName = name().toLowerCase();
    }

    Repository() {
        this(new String[0]);
    }

    public RepoList getList() {
        return repoList;
    }

    public RepoList.RelevantRepoList getRelevant() {
        return repoList.getRelevant();
    }

    public InputStream get(String path) throws IOException {
        return repoList.read(path);
    }

    public InputStreamReader read(String path) throws IOException {
        return new InputStreamReader(get(path), FileUtil.DEFAULT_CHARSET);
    }

    public boolean isRemote() {
        return getRelevant().getFirst() != null;
    }
}
