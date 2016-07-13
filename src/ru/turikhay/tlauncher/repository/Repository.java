package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

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

   private Repository(String[] urlList) {
      this.repoList = new AppenderRepoList(this.name(), (String[])U.requireNotNull(urlList, "urlList"));
      this.lowerName = this.name().toLowerCase();
   }

   private Repository() {
      this(new String[0]);
   }

   public RepoList getList() {
      return this.repoList;
   }

   public RepoList.RelevantRepoList getRelevant() {
      return this.repoList.getRelevant();
   }

   public InputStream get(String path) throws IOException {
      return this.repoList.read(path);
   }

   public InputStreamReader read(String path) throws IOException {
      return new InputStreamReader(this.get(path), "UTF-8");
   }

   public boolean isRemote() {
      return this.getRelevant().getFirst() != null;
   }
}
