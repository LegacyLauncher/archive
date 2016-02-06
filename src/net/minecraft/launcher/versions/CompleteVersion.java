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
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.launcher.updater.AssetIndexInfo;
import net.minecraft.launcher.updater.DownloadInfo;
import net.minecraft.launcher.updater.DownloadType;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class CompleteVersion implements Cloneable, Version {
   String id;
   String jar;
   String family;
   String inheritsFrom;
   Date time;
   Date releaseTime;
   ReleaseType type;
   String jvmArguments;
   String minecraftArguments;
   String mainClass;
   Integer minimumLauncherVersion = 0;
   Integer tlauncherVersion = 0;
   String assets;
   Repository source;
   VersionList list;
   List libraries;
   List rules;
   List deleteEntries;
   Map downloads = new HashMap();
   AssetIndexInfo assetIndex;
   protected static final Pattern familyPattern = Pattern.compile("([a-z]*[\\d]\\.[\\d]+).*");

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

   public String getFamily() {
      return this.family;
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

   public String getJar() {
      return this.jar;
   }

   public String getInheritsFrom() {
      return this.inheritsFrom;
   }

   public String getJVMArguments() {
      return this.jvmArguments;
   }

   public String getMinecraftArguments() {
      return this.minecraftArguments;
   }

   public void setMinecraftArguments(String args) {
      this.minecraftArguments = args;
   }

   public String getMainClass() {
      return this.mainClass;
   }

   public void setMainClass(String clazz) {
      this.mainClass = clazz;
   }

   public List getLibraries() {
      return this.libraries;
   }

   public List getDeleteEntries() {
      return this.deleteEntries;
   }

   public int getMinimumLauncherVersion() {
      return this.minimumLauncherVersion;
   }

   public int getMinimumCustomLauncherVersion() {
      return this.tlauncherVersion;
   }

   public AssetIndexInfo getAssetIndex() {
      if (this.assetIndex == null) {
         this.assetIndex = new AssetIndexInfo(this.assets == null ? "legacy" : this.assets);
      }

      return this.assetIndex;
   }

   public DownloadInfo getDownloadURL(DownloadType type) {
      return (DownloadInfo)this.downloads.get(type);
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
      return this.getClass().getSimpleName() + this.debugString();
   }

   public String debugString() {
      return "{id='" + this.id + "', time=" + this.time + ", release=" + this.releaseTime + ", type=" + this.type + ", class=" + this.mainClass + ", minimumVersion=" + this.minimumLauncherVersion + ", assets='" + this.assets + "', source=" + this.source + ", list=" + this.list + ", libraries=" + this.libraries + "}";
   }

   public File getFile(File base) {
      return new File(base, "versions/" + this.getID() + "/" + this.getID() + ".jar");
   }

   public boolean appliesToCurrentEnvironment() {
      if (this.rules == null) {
         return true;
      } else {
         Iterator var2 = this.rules.iterator();

         Rule.Action action;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            Rule rule = (Rule)var2.next();
            action = rule.getAppliedAction();
         } while(action != Rule.Action.DISALLOW);

         return false;
      }
   }

   public Collection getRelevantLibraries() {
      ArrayList result = new ArrayList();
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
      ArrayList result = new ArrayList();
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
      ArrayList result = new ArrayList();
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

   public CompleteVersion resolve(VersionManager vm, boolean useLatest) throws IOException {
      return this.resolve(vm, useLatest, new ArrayList());
   }

   protected CompleteVersion resolve(VersionManager vm, boolean useLatest, List inheristance) throws IOException {
      if (vm == null) {
         throw new NullPointerException("version manager");
      } else if (this.inheritsFrom != null) {
         if (inheristance.contains(this.id)) {
            throw new CompleteVersion.DuplicateInheritanceException();
         } else {
            inheristance.add(this.id);
            VersionSyncInfo parentSyncInfo = vm.getVersionSyncInfo(this.inheritsFrom);
            if (parentSyncInfo == null) {
               throw new CompleteVersion.ParentNotFoundException();
            } else {
               CompleteVersion result;
               try {
                  result = (CompleteVersion)parentSyncInfo.getCompleteVersion(useLatest).resolve(vm, useLatest, inheristance).clone();
               } catch (CloneNotSupportedException var7) {
                  throw new RuntimeException(var7);
               }

               if (this.id.toLowerCase().contains("forge") && this.family == null && result.family != null && !result.family.startsWith("Forge-")) {
                  this.family = "Forge-" + result.family;
               }

               return this.copyInto(result);
            }
         }
      } else {
         if (this.family == null || this.family.equals("Forge-")) {
            String family_;
            switch(this.type) {
            case UNKNOWN:
            case OLD_ALPHA:
            case SNAPSHOT:
               family_ = this.type.toString();
               break;
            default:
               family_ = getFamilyOf(this.id);
               if (family_ == null && this.jar != null) {
                  family_ = getFamilyOf(this.jar);
               }

               if (family_ == null && this.inheritsFrom != null) {
                  family_ = getFamilyOf(this.inheritsFrom);
               }
            }

            if (family_ == null) {
               family_ = "unknown";
            }

            this.family = family_;
         }

         return this;
      }
   }

   public CompleteVersion copyInto(CompleteVersion result) {
      result.id = this.id;
      if (this.jar != null) {
         result.jar = this.jar;
      }

      if (this.family != null) {
         result.family = this.family;
      }

      result.inheritsFrom = null;
      if (this.time.getTime() != 0L) {
         result.time = this.time;
      }

      if (this.type != ReleaseType.UNKNOWN) {
         result.type = this.type;
      }

      if (this.jvmArguments != null) {
         result.jvmArguments = this.jvmArguments;
      }

      if (this.minecraftArguments != null) {
         result.minecraftArguments = this.minecraftArguments;
      }

      if (this.mainClass != null) {
         result.mainClass = this.mainClass;
      }

      ArrayList rulesCopy;
      if (this.libraries != null) {
         rulesCopy = new ArrayList();
         rulesCopy.addAll(this.libraries);
         if (result.libraries != null) {
            rulesCopy.addAll(result.libraries);
         }

         result.libraries = rulesCopy;
      }

      if (this.rules != null) {
         if (result.rules != null) {
            result.rules.addAll(this.rules);
         } else {
            rulesCopy = new ArrayList(this.rules.size());
            Collections.copy(rulesCopy, this.rules);
            result.rules = this.rules;
         }
      }

      if (this.deleteEntries != null) {
         if (result.deleteEntries != null) {
            result.deleteEntries.addAll(this.deleteEntries);
         } else {
            result.deleteEntries = new ArrayList(this.deleteEntries);
         }
      }

      if (this.minimumLauncherVersion != 0) {
         result.minimumLauncherVersion = this.minimumLauncherVersion;
      }

      if (this.tlauncherVersion != 0) {
         result.tlauncherVersion = this.tlauncherVersion;
      }

      if (this.assets != null && !this.assets.equals("legacy")) {
         result.assets = this.assets;
      }

      if (this.source != null) {
         result.source = this.source;
      }

      result.list = this.list;
      return result;
   }

   private static String getFamilyOf(String id) {
      Matcher matcher = familyPattern.matcher(id);
      String family;
      if (matcher.matches()) {
         family = matcher.group(1);
      } else {
         family = null;
      }

      return family;
   }

   public class ParentNotFoundException extends CompleteVersion.InheritanceException {
      public ParentNotFoundException() {
         super();
      }
   }

   public class InheritanceException extends IOException {
      InheritanceException() {
         super(CompleteVersion.this.id + " should inherit from " + CompleteVersion.this.inheritsFrom);
      }
   }

   public class DuplicateInheritanceException extends CompleteVersion.InheritanceException {
      public DuplicateInheritanceException() {
         super();
      }
   }

   public static class CompleteVersionSerializer implements JsonDeserializer, JsonSerializer {
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
         JsonElement originalId = object.get("original_id");
         if (originalId != null && originalId.isJsonPrimitive()) {
            String unnecessaryEntries = originalId.getAsString();
            object.remove("original_id");
            object.addProperty("jar", unnecessaryEntries);
         }

         JsonElement unnecessaryEntries1 = object.get("unnecessaryEntries");
         if (unnecessaryEntries1 != null && unnecessaryEntries1.isJsonArray()) {
            object.remove("unnecessaryEntries");
            object.add("deleteEntries", unnecessaryEntries1);
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
         } catch (CloneNotSupportedException var7) {
            U.log("Cloning of CompleteVersion is not supported O_o", var7);
            return this.defaultContext.toJsonTree(version0, type);
         }

         version.list = null;
         JsonObject object = (JsonObject)this.defaultContext.toJsonTree(version, type);
         JsonElement jar = object.get("jar");
         if (jar == null) {
            object.remove("downloadJarLibraries");
         }

         return object;
      }
   }
}
