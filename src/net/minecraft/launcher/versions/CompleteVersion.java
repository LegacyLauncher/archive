package net.minecraft.launcher.versions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class CompleteVersion implements Version, Cloneable {
   String id;
   String original_id;
   Date time;
   Date releaseTime;
   ReleaseType type;
   String jvmArguments;
   String minecraftArguments;
   String mainClass;
   List libraries;
   List rules;
   List unnecessaryEntries;
   Integer minimumLauncherVersion = 0;
   Integer tlauncherVersion = 0;
   String assets;
   Repository source;
   VersionList list;

   public String getID() {
      return this.id;
   }

   public void setID(String id) {
      if (id != null && !id.isEmpty()) {
         this.id = id;
      } else {
         throw new IllegalArgumentException("ID is NULL or empty");
      }
   }

   public ReleaseType getReleaseType() {
      return this.type;
   }

   public Repository getSource() {
      return this.source;
   }

   public void setSource(Repository repository) {
      if (repository == null) {
         throw new NullPointerException();
      } else {
         this.source = repository;
      }
   }

   public Date getUpdatedTime() {
      return this.time;
   }

   public void setUpdatedTime(Date time) {
      if (time == null) {
         throw new NullPointerException("Time is NULL!");
      } else {
         this.time = time;
      }
   }

   public Date getReleaseTime() {
      return this.releaseTime;
   }

   public VersionList getVersionList() {
      return this.list;
   }

   public void setVersionList(VersionList list) {
      if (list == null) {
         throw new NullPointerException("VersionList cannot be NULL!");
      } else {
         this.list = list;
      }
   }

   public String getOriginal() {
      return this.original_id;
   }

   public String getJVMArguments() {
      return this.jvmArguments;
   }

   public String getMinecraftArguments() {
      return this.minecraftArguments;
   }

   public String getMainClass() {
      return this.mainClass;
   }

   public List getLibraries() {
      return Collections.unmodifiableList(this.libraries);
   }

   public List getRules() {
      return Collections.unmodifiableList(this.rules);
   }

   public List getRemovableEntries() {
      return this.unnecessaryEntries;
   }

   public int getMinimumLauncherVersion() {
      return this.minimumLauncherVersion;
   }

   public int getMinimumCustomLauncherVersion() {
      return this.tlauncherVersion;
   }

   public String getAssets() {
      return this.assets;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o == null) {
         return false;
      } else if (this.hashCode() == o.hashCode()) {
         return true;
      } else if (!(o instanceof Version)) {
         return false;
      } else {
         Version compare = (Version)o;
         return compare.getID() == null ? false : compare.getID().equals(this.id);
      }
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{id='" + this.id + "', time=" + this.time + ", release=" + this.releaseTime + ", type=" + this.type + ", class=" + this.mainClass + ", minimumVersion=" + this.minimumLauncherVersion + ", assets='" + this.assets + "', source=" + this.source + ", list=" + this.list + ", libraries=" + this.libraries + "}";
   }

   public File getFile(File base) {
      return new File(base, "versions/" + this.getID() + "/" + this.getID() + ".jar");
   }

   public boolean appliesToCurrentEnvironment() {
      if (this.rules == null) {
         return true;
      } else {
         Iterator var2 = this.rules.iterator();

         while(var2.hasNext()) {
            Rule rule = (Rule)var2.next();
            Rule.Action action = rule.getAppliedAction();
            if (action == Rule.Action.DISALLOW) {
               return false;
            }
         }

         return true;
      }
   }

   public Collection getRelevantLibraries() {
      List result = new ArrayList();
      Iterator var3 = this.libraries.iterator();

      while(var3.hasNext()) {
         Library library = (Library)var3.next();
         if (library.appliesToCurrentEnvironment()) {
            result.add(library);
         }
      }

      return result;
   }

   public Collection getClassPath(OS os, File base) {
      Collection libraries = this.getRelevantLibraries();
      Collection result = new ArrayList();
      Iterator var6 = libraries.iterator();

      while(var6.hasNext()) {
         Library library = (Library)var6.next();
         if (library.getNatives() == null) {
            result.add(new File(base, "libraries/" + library.getArtifactPath()));
         }
      }

      result.add(new File(base, "versions/" + this.getID() + "/" + this.getID() + ".jar"));
      return result;
   }

   public Collection getClassPath(File base) {
      return this.getClassPath(OS.CURRENT, base);
   }

   public Collection getNatives(OS os) {
      Collection libraries = this.getRelevantLibraries();
      Collection result = new ArrayList();
      Iterator var5 = libraries.iterator();

      while(var5.hasNext()) {
         Library library = (Library)var5.next();
         Map natives = library.getNatives();
         if (natives != null && natives.containsKey(os)) {
            result.add("libraries/" + library.getArtifactPath((String)natives.get(os)));
         }
      }

      return result;
   }

   public Collection getNatives() {
      return this.getNatives(OS.CURRENT);
   }

   public Set getRequiredFiles(OS os) {
      Set neededFiles = new HashSet();
      Iterator var4 = this.getRelevantLibraries().iterator();

      while(var4.hasNext()) {
         Library library = (Library)var4.next();
         if (library.getNatives() != null) {
            String natives = (String)library.getNatives().get(os);
            if (natives != null) {
               neededFiles.add("libraries/" + library.getArtifactPath(natives));
            }
         } else {
            neededFiles.add("libraries/" + library.getArtifactPath());
         }
      }

      return neededFiles;
   }

   public Object cloneSafely() {
      try {
         return super.clone();
      } catch (CloneNotSupportedException var2) {
         return null;
      }
   }

   public Collection getExtractFiles(OS os) {
      Collection libraries = this.getRelevantLibraries();
      Collection result = new ArrayList();
      Iterator var5 = libraries.iterator();

      while(var5.hasNext()) {
         Library library = (Library)var5.next();
         Map natives = library.getNatives();
         if (natives != null && natives.containsKey(os)) {
            result.add("libraries/" + library.getArtifactPath((String)natives.get(os)));
         }
      }

      return result;
   }

   public static class CompleteVersionSerializer implements JsonSerializer, JsonDeserializer {
      private final Gson defaultContext;

      public CompleteVersionSerializer() {
         GsonBuilder builder = new GsonBuilder();
         builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
         builder.enableComplexMapKeySerialization();
         builder.setPrettyPrinting();
         this.defaultContext = builder.create();
      }

      public CompleteVersion deserialize(JsonElement elem, Type type, JsonDeserializationContext context) throws JsonParseException {
         JsonObject object = elem.getAsJsonObject();
         JsonElement sourceElement = object.get("source");
         if (sourceElement != null && !sourceElement.isJsonPrimitive()) {
            object.remove("source");
         }

         CompleteVersion version = (CompleteVersion)this.defaultContext.fromJson(elem, CompleteVersion.class);
         if (version.id == null) {
            throw new JsonParseException("Version ID is NULL!");
         } else {
            if (version.type == null) {
               version.type = ReleaseType.UNKNOWN;
            }

            if (version.source == null) {
               version.source = Repository.LOCAL_VERSION_REPO;
            }

            if (version.time == null) {
               version.time = new Date(0L);
            }

            if (version.assets == null) {
               version.assets = "legacy";
            }

            return version;
         }
      }

      public JsonElement serialize(CompleteVersion version0, Type type, JsonSerializationContext context) {
         CompleteVersion version;
         try {
            version = (CompleteVersion)version0.clone();
         } catch (CloneNotSupportedException var6) {
            U.log("Cloning of CompleteVersion is not supported O_o", var6);
            return this.defaultContext.toJsonTree(version0, type);
         }

         version.list = null;
         return this.defaultContext.toJsonTree(version, type);
      }
   }
}
