package ru.turikhay.tlauncher.repository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.launcher.Http;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

public enum Repository {
   LOCAL_VERSION_REPO,
   OFFICIAL_VERSION_REPO(TLauncher.getOfficialRepo()),
   EXTRA_VERSION_REPO(TLauncher.getExtraRepo()),
   ASSETS_REPO(TLauncher.getAssetsRepo()),
   LIBRARY_REPO(TLauncher.getLibraryRepo()),
   SERVERLIST_REPO(TLauncher.getServerList());

   private static final int DEFAULT_TIMEOUT = 5000;
   public static final Repository[] VERSION_REPOS = getVersionRepos();
   private final String lowerName;
   private final List repos;
   private int primaryTimeout;
   private int selected;
   private boolean isSelected;

   private Repository(int timeout, String[] urls) {
      if (urls == null) {
         throw new NullPointerException("URL array is NULL!");
      } else {
         this.lowerName = super.name().toLowerCase();
         this.repos = new ArrayList();
         this.setTimeout(timeout);
         Collections.addAll(this.repos, urls);
      }
   }

   private Repository(String[] urls) {
      this(5000, urls);
   }

   private Repository(int timeout) {
      this(timeout, new String[0]);
   }

   private Repository() {
      this(5000, new String[0]);
   }

   public int getTimeout() {
      return this.primaryTimeout;
   }

   int getSelected() {
      return this.selected;
   }

   public synchronized void selectNext() {
      if (++this.selected >= this.getCount()) {
         this.selected = 0;
      }

   }

   void setSelected(int pos) {
      if (!this.isSelectable()) {
         throw new IllegalStateException();
      } else {
         this.isSelected = true;
         this.selected = pos;
      }
   }

   public String getSelectedRepo() {
      return (String)this.repos.get(this.selected);
   }

   String getRepo(int pos) {
      return (String)this.repos.get(pos);
   }

   public List getList() {
      return this.repos;
   }

   public int getCount() {
      return this.repos.size();
   }

   boolean isSelected() {
      return this.isSelected;
   }

   public boolean isSelectable() {
      return !this.repos.isEmpty();
   }

   String getUrl(String uri, boolean selectPath) throws IOException {
      boolean canSelect = this.isSelectable();
      if (!canSelect) {
         return this.getRawUrl(uri);
      } else {
         boolean gotError = false;
         if (!selectPath && this.isSelected()) {
            try {
               return this.getRawUrl(uri);
            } catch (IOException var14) {
               gotError = true;
               this.log("Cannot get required URL, reselecting path.");
            }
         }

         this.log("Selecting relevant path...");
         Object lock = new Object();
         IOException e = null;
         int i = 0;
         int attempt = 0;
         int exclude = gotError ? this.getSelected() : -1;

         while(i < 3) {
            ++i;
            int timeout = this.primaryTimeout * i;

            for(int x = 0; x < this.getCount(); ++x) {
               if (i != 1 || x != exclude) {
                  ++attempt;
                  this.log("Attempt #" + attempt + "; timeout: " + timeout + " ms; url: " + this.getRepo(x));
                  Time.start(lock);

                  try {
                     String result = Http.performGet(new URL(this.getRepo(x) + uri), timeout, timeout);
                     this.setSelected(x);
                     this.log("Success: Reached the repo in", Time.stop(lock), "ms.");
                     return result;
                  } catch (IOException var13) {
                     this.log("Failed: Repo is not reachable!", var13);
                     e = var13;
                     Time.stop(lock);
                  }
               }
            }
         }

         this.log("Failed: All repos are unreachable.");
         throw e;
      }
   }

   public String getUrl(String uri) throws IOException {
      return this.getUrl(uri, false);
   }

   public String getUrl() throws IOException {
      return this.getUrl("", false);
   }

   String getRawUrl(String uri) throws IOException {
      String url = this.getSelectedRepo() + Http.encode(uri);

      try {
         return Http.performGet(new URL(url));
      } catch (IOException var4) {
         this.log("Cannot get raw:", url);
         throw var4;
      }
   }

   public String toString() {
      return this.lowerName;
   }

   void setTimeout(int ms) {
      if (ms < 0) {
         throw new IllegalArgumentException("Negative timeout: " + ms);
      } else {
         this.primaryTimeout = ms;
      }
   }

   void log(Object... obj) {
      U.log("[REPO][" + this.name() + "]", obj);
   }

   public static Repository[] getVersionRepos() {
      return new Repository[]{LOCAL_VERSION_REPO, OFFICIAL_VERSION_REPO, EXTRA_VERSION_REPO};
   }
}
