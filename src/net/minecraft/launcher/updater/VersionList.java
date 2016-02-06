package net.minecraft.launcher.updater;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.PartialVersion;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

public abstract class VersionList {
   protected final Gson gson;
   protected final Map byName = new Hashtable();
   protected final List versions = Collections.synchronizedList(new ArrayList());
   protected final Map latest = new Hashtable();

   VersionList() {
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
      builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
      builder.registerTypeAdapter(CompleteVersion.class, new CompleteVersion.CompleteVersionSerializer());
      builder.enableComplexMapKeySerialization();
      builder.setPrettyPrinting();
      this.gson = builder.create();
   }

   public List getVersions() {
      return this.versions;
   }

   public Map getLatestVersions() {
      return this.latest;
   }

   public Version getVersion(String name) {
      if (name != null && !name.isEmpty()) {
         return (Version)this.byName.get(name);
      } else {
         throw new IllegalArgumentException("Name cannot be NULL or empty");
      }
   }

   public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
      if (version instanceof CompleteVersion) {
         return (CompleteVersion)version;
      } else if (version == null) {
         throw new NullPointerException("Version cannot be NULL!");
      } else {
         CompleteVersion complete = (CompleteVersion)this.gson.fromJson(this.getUrl("versions/" + version.getID() + "/" + version.getID() + ".json"), CompleteVersion.class);
         complete.setID(version.getID());
         complete.setVersionList(this);
         Collections.replaceAll(this.versions, version, complete);
         return complete;
      }
   }

   public CompleteVersion getCompleteVersion(String name) throws JsonSyntaxException, IOException {
      Version version = this.getVersion(name);
      return version == null ? null : this.getCompleteVersion(version);
   }

   public VersionList.RawVersionList getRawList() throws IOException {
      Object lock = new Object();
      Time.start(lock);
      VersionList.RawVersionList list = (VersionList.RawVersionList)this.gson.fromJson(this.getUrl("versions/versions.json"), VersionList.RawVersionList.class);
      Iterator var4 = list.versions.iterator();

      while(var4.hasNext()) {
         PartialVersion version = (PartialVersion)var4.next();
         version.setVersionList(this);
      }

      this.log("Got in", Time.stop(lock), "ms");
      return list;
   }

   public void refreshVersions(VersionList.RawVersionList versionList) {
      this.clearCache();
      Iterator var3 = versionList.getVersions().iterator();

      while(var3.hasNext()) {
         Version en = (Version)var3.next();
         if (en != null && en.getID() != null) {
            this.versions.add(en);
            this.byName.put(en.getID(), en);
         }
      }

      var3 = versionList.latest.entrySet().iterator();

      while(var3.hasNext()) {
         Entry en1 = (Entry)var3.next();
         ReleaseType releaseType = (ReleaseType)en1.getKey();
         if (releaseType == null) {
            this.log("Unknown release type for latest version entry:", en1);
         } else {
            Version version = this.getVersion((String)en1.getValue());
            if (version == null) {
               this.log("Cannot find version for latest version entry: " + en1);
            } else {
               this.latest.put(releaseType, version);
            }
         }
      }

   }

   public void refreshVersions() throws IOException {
      this.refreshVersions(this.getRawList());
   }

   CompleteVersion addVersion(CompleteVersion version) {
      if (version.getID() == null) {
         throw new IllegalArgumentException("Cannot add blank version");
      } else if (this.getVersion(version.getID()) != null) {
         this.log("Version '" + version.getID() + "' is already tracked");
         return version;
      } else {
         this.versions.add(version);
         this.byName.put(version.getID(), version);
         return version;
      }
   }

   public String serializeVersion(CompleteVersion version) {
      if (version == null) {
         throw new NullPointerException("CompleteVersion cannot be NULL!");
      } else {
         return this.gson.toJson((Object)version);
      }
   }

   protected abstract String getUrl(String var1) throws IOException;

   void clearCache() {
      this.byName.clear();
      this.versions.clear();
      this.latest.clear();
   }

   void log(Object... obj) {
      U.log("[" + this.getClass().getSimpleName() + "]", obj);
   }

   public static class RawVersionList {
      List versions = new ArrayList();
      Map latest = new EnumMap(ReleaseType.class);

      public List getVersions() {
         return this.versions;
      }
   }
}
