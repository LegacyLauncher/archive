package net.minecraft.launcher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.updater.versions.json.DateTypeAdapter;
import net.minecraft.launcher.updater.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.VersionSource;

public abstract class VersionList {
   public static final int DEFAULT_TIMEOUT = 7500;
   protected final Gson gson;
   private final Map versionsByName = new HashMap();
   private final List versions = new ArrayList();
   private final Map latestVersions = new EnumMap(ReleaseType.class);

   public VersionList() {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
      builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
      builder.enableComplexMapKeySerialization();
      builder.setPrettyPrinting();
      this.gson = builder.create();
   }

   public Collection getVersions() {
      return this.versions;
   }

   public Version getLatestVersion(ReleaseType type) {
      if (type == null) {
         throw new IllegalArgumentException("Type cannot be null");
      } else {
         return (Version)this.latestVersions.get(type);
      }
   }

   public Version getVersion(String name) {
      if (name != null && name.length() != 0) {
         return (Version)this.versionsByName.get(name);
      } else {
         throw new IllegalArgumentException("Name cannot be null or empty");
      }
   }

   public CompleteVersion getCompleteVersion(String name) throws IOException {
      if (name != null && name.length() != 0) {
         Version version = this.getVersion(name);
         if (version == null) {
            throw new IllegalArgumentException("Unknown version - cannot get complete version of null");
         } else {
            return this.getCompleteVersion(version);
         }
      } else {
         throw new IllegalArgumentException("Name cannot be null or empty");
      }
   }

   public CompleteVersion getCompleteVersion(Version version) throws IOException {
      if (version instanceof CompleteVersion) {
         return (CompleteVersion)version;
      } else if (version == null) {
         throw new IllegalArgumentException("Version cannot be null");
      } else {
         CompleteVersion complete = (CompleteVersion)this.gson.fromJson(this.getUrl("versions/" + version.getId() + "/" + version.getId() + ".json"), CompleteVersion.class);
         ReleaseType type = version.getType();
         Collections.replaceAll(this.versions, version, complete);
         this.versionsByName.put(version.getId(), complete);
         if (this.latestVersions.get(type) == version) {
            this.latestVersions.put(type, complete);
         }

         return complete;
      }
   }

   protected void clearCache() {
      this.versionsByName.clear();
      this.versions.clear();
      this.latestVersions.clear();
   }

   public VersionList.RawVersionList getRawList() throws IOException {
      Object lock = new Object();
      Time.start(lock);
      VersionList.RawVersionList list = (VersionList.RawVersionList)this.gson.fromJson(this.getUrl("versions/versions.json"), VersionList.RawVersionList.class);
      this.log("Got in", Time.stop(lock), "ms");
      return list;
   }

   public void refreshVersions(VersionList.RawVersionList versionList) {
      this.clearCache();
      Iterator var3 = versionList.getVersions().iterator();

      while(var3.hasNext()) {
         Version version = (Version)var3.next();
         this.versions.add(version);
         this.versionsByName.put(version.getId(), version);
      }

      ReleaseType[] var5;
      int var4 = (var5 = ReleaseType.values()).length;

      for(int var7 = 0; var7 < var4; ++var7) {
         ReleaseType type = var5[var7];
         this.latestVersions.put(type, (Version)this.versionsByName.get(versionList.getLatestVersions().get(type)));
      }

   }

   public void refreshVersions() throws IOException {
      this.refreshVersions(this.getRawList());
   }

   public CompleteVersion addVersion(CompleteVersion version) {
      if (version.getId() == null) {
         throw new IllegalArgumentException("Cannot add blank version");
      } else if (this.getVersion(version.getId()) != null) {
         this.log("Version '" + version.getId() + "' is already tracked");
         return version;
      } else {
         this.versions.add(version);
         this.versionsByName.put(version.getId(), version);
         return version;
      }
   }

   public void removeVersion(String name) {
      if (name != null && name.length() != 0) {
         Version version = this.getVersion(name);
         if (version == null) {
            throw new IllegalArgumentException("Unknown version - cannot remove null");
         } else {
            this.removeVersion(version);
         }
      } else {
         throw new IllegalArgumentException("Name cannot be null or empty");
      }
   }

   public void removeVersion(Version version) {
      if (version == null) {
         throw new IllegalArgumentException("Cannot remove null version");
      } else {
         this.versions.remove(version);
         this.versionsByName.remove(version.getId());
         ReleaseType[] var5;
         int var4 = (var5 = ReleaseType.values()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            ReleaseType type = var5[var3];
            if (this.getLatestVersion(type) == version) {
               this.latestVersions.remove(type);
            }
         }

      }
   }

   public void setLatestVersion(Version version) {
      if (version == null) {
         throw new IllegalArgumentException("Cannot set latest version to null");
      } else {
         this.latestVersions.put(version.getType(), version);
      }
   }

   public void setLatestVersion(String name) {
      if (name != null && name.length() != 0) {
         Version version = this.getVersion(name);
         if (version == null) {
            throw new IllegalArgumentException("Unknown version - cannot set latest version to null");
         } else {
            this.setLatestVersion(version);
         }
      } else {
         throw new IllegalArgumentException("Name cannot be null or empty");
      }
   }

   public String serializeVersionList() {
      VersionList.RawVersionList list = new VersionList.RawVersionList();
      ReleaseType[] var5;
      int var4 = (var5 = ReleaseType.values()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         ReleaseType type = var5[var3];
         Version latest = this.getLatestVersion(type);
         if (latest != null) {
            list.getLatestVersions().put(type, latest.getId());
         }
      }

      PartialVersion partial;
      for(Iterator var8 = this.getVersions().iterator(); var8.hasNext(); list.getVersions().add(partial)) {
         Version version = (Version)var8.next();
         partial = null;
         if (version instanceof PartialVersion) {
            partial = (PartialVersion)version;
         } else {
            partial = new PartialVersion(version);
         }
      }

      return this.gson.toJson((Object)list);
   }

   public String serializeVersion(CompleteVersion version) {
      if (version == null) {
         throw new IllegalArgumentException("Cannot serialize null!");
      } else {
         return this.gson.toJson((Object)version);
      }
   }

   protected String getUrl(String uri, boolean selectPath) throws IOException {
      VersionSource source = this.getRepositoryType();
      boolean canSelect = source.isSelectable();
      if (!canSelect) {
         return this.getRawUrl(uri);
      } else {
         boolean gotError = false;
         if (!selectPath && source.isSelected()) {
            try {
               return this.getRawUrl(uri);
            } catch (IOException var15) {
               gotError = true;
               this.log("Cannot get required URL, reselecting path.");
            }
         }

         this.log("Selecting relevant path...");
         Object lock = new Object();
         IOException e = null;
         int i = 0;
         int attempt = 0;
         int exclude = gotError ? source.getSelected() : -1;

         while(i < 3) {
            ++i;
            int timeout = 7500 * i;

            for(int x = 0; x < source.getRepoCount(); ++x) {
               if (i != 1 || x != exclude) {
                  ++attempt;
                  this.log("Attempt #" + attempt + "; timeout: " + timeout + " ms; url: " + source.getRepo(x));
                  Time.start(lock);

                  try {
                     String result = Http.performGet(new URL(source.getRepo(x) + uri), timeout, timeout);
                     source.setSelected(x);
                     this.log("Success: Reached the repo in", Time.stop(lock), "ms.");
                     return result;
                  } catch (IOException var14) {
                     this.log("Failed: Repo is not reachable!");
                     e = var14;
                     Time.stop(lock);
                  }
               }
            }
         }

         this.log("Failed: All repos are unreachable.");
         throw e;
      }
   }

   protected String getUrl(String uri) throws IOException {
      return this.getUrl(uri, false);
   }

   protected String getRawUrl(String uri) throws IOException {
      String url = this.getRepositoryType().getSelectedRepo() + Http.encode(uri);

      try {
         return Http.performGet(new URL(url));
      } catch (IOException var4) {
         this.log("Cannot get raw:", url);
         throw var4;
      }
   }

   public abstract boolean hasAllFiles(CompleteVersion var1, OperatingSystem var2);

   public abstract VersionSource getRepositoryType();

   protected void log(Object... obj) {
      U.log("[" + this.getClass().getSimpleName() + "]", obj);
   }

   public static class RawVersionList {
      private List versions = new ArrayList();
      private Map latest = new EnumMap(ReleaseType.class);

      public List getVersions() {
         return this.versions;
      }

      public Map getLatestVersions() {
         return this.latest;
      }
   }
}
