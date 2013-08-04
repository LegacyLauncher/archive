package net.minecraft.launcher_.versions;

import com.turikhay.tlauncher.downloader.Downloadable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.launcher_.OperatingSystem;

public class CompleteVersion implements Version {
   private String id;
   private Date time;
   private Date releaseTime;
   private ReleaseType type;
   private String minecraftArguments;
   private List libraries;
   private String mainClass;
   private int minimumLauncherVersion;
   private String incompatibilityReason;
   private List rules;

   public CompleteVersion() {
   }

   public CompleteVersion(String id, Date releaseTime, Date updateTime, ReleaseType type, String mainClass, String minecraftArguments) {
      if (id != null && id.length() != 0) {
         if (releaseTime == null) {
            throw new IllegalArgumentException("Release time cannot be null");
         } else if (updateTime == null) {
            throw new IllegalArgumentException("Update time cannot be null");
         } else if (type == null) {
            throw new IllegalArgumentException("Release type cannot be null");
         } else if (mainClass != null && mainClass.length() != 0) {
            if (minecraftArguments == null) {
               throw new IllegalArgumentException("Process arguments cannot be null or empty");
            } else {
               this.id = id;
               this.releaseTime = releaseTime;
               this.time = updateTime;
               this.type = type;
               this.mainClass = mainClass;
               this.libraries = new ArrayList();
               this.minecraftArguments = minecraftArguments;
            }
         } else {
            throw new IllegalArgumentException("Main class cannot be null or empty");
         }
      } else {
         throw new IllegalArgumentException("ID cannot be null or empty");
      }
   }

   public CompleteVersion(CompleteVersion version) {
      this(version.getId(), version.getReleaseTime(), version.getUpdatedTime(), version.getType(), version.getMainClass(), version.getMinecraftArguments());
   }

   public CompleteVersion(Version version, String mainClass, String minecraftArguments) {
      this(version.getId(), version.getReleaseTime(), version.getUpdatedTime(), version.getType(), mainClass, minecraftArguments);
   }

   public String getId() {
      return this.id;
   }

   public ReleaseType getType() {
      return this.type;
   }

   public Date getUpdatedTime() {
      return this.time;
   }

   public Date getReleaseTime() {
      return this.releaseTime;
   }

   public Collection getLibraries() {
      return this.libraries;
   }

   public String getMainClass() {
      return this.mainClass;
   }

   public void setUpdatedTime(Date time) {
      if (time == null) {
         throw new IllegalArgumentException("Time cannot be null");
      } else {
         this.time = time;
      }
   }

   public void setReleaseTime(Date time) {
      if (time == null) {
         throw new IllegalArgumentException("Time cannot be null");
      } else {
         this.releaseTime = time;
      }
   }

   public void setType(ReleaseType type) {
      if (type == null) {
         throw new IllegalArgumentException("Release type cannot be null");
      } else {
         this.type = type;
      }
   }

   public void setMainClass(String mainClass) {
      if (mainClass != null && mainClass.length() != 0) {
         this.mainClass = mainClass;
      } else {
         throw new IllegalArgumentException("Main class cannot be null or empty");
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

   public Collection getClassPath(OperatingSystem os, File base) {
      Collection libraries = this.getRelevantLibraries();
      Collection result = new ArrayList();
      Iterator var6 = libraries.iterator();

      while(var6.hasNext()) {
         Library library = (Library)var6.next();
         if (library.getNatives() == null) {
            result.add(new File(base, "libraries/" + library.getArtifactPath()));
         }
      }

      result.add(new File(base, "versions/" + this.getId() + "/" + this.getId() + ".jar"));
      return result;
   }

   public Collection getExtractFiles(OperatingSystem os) {
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

   public Set getRequiredFiles(OperatingSystem os) {
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

   public Set getRequiredDownloadables(OperatingSystem os, File targetDirectory, boolean ignoreLocalFiles) throws MalformedURLException {
      Set neededFiles = new HashSet();
      Iterator var6 = this.getRelevantLibraries().iterator();

      while(true) {
         Library library;
         File local;
         URL url;
         do {
            String file;
            do {
               if (!var6.hasNext()) {
                  return neededFiles;
               }

               library = (Library)var6.next();
               file = null;
               if (library.getNatives() != null) {
                  String natives = (String)library.getNatives().get(os);
                  if (natives != null) {
                     file = library.getArtifactPath(natives);
                  }
               } else {
                  file = library.getArtifactPath();
               }
            } while(file == null);

            url = new URL(library.getDownloadUrl() + file);
            local = new File(targetDirectory, "libraries/" + file);
         } while(local.isFile() && library.hasCustomUrl());

         neededFiles.add(new Downloadable(url, local, ignoreLocalFiles));
      }
   }

   public String toString() {
      return "CompleteVersion{id='" + this.id + '\'' + ", time=" + this.time + ", type=" + this.type + ", libraries=" + this.libraries + ", mainClass='" + this.mainClass + '\'' + ", minimumLauncherVersion=" + this.minimumLauncherVersion + '}';
   }

   public String getMinecraftArguments() {
      return this.minecraftArguments;
   }

   public void setMinecraftArguments(String minecraftArguments) {
      if (minecraftArguments == null) {
         throw new IllegalArgumentException("Process arguments cannot be null or empty");
      } else {
         this.minecraftArguments = minecraftArguments;
      }
   }

   public int getMinimumLauncherVersion() {
      return this.minimumLauncherVersion;
   }

   public void setMinimumLauncherVersion(int minimumLauncherVersion) {
      this.minimumLauncherVersion = minimumLauncherVersion;
   }

   public boolean appliesToCurrentEnvironment() {
      if (this.rules == null) {
         return true;
      } else {
         Rule.Action lastAction = Rule.Action.DISALLOW;
         Iterator var3 = this.rules.iterator();

         while(var3.hasNext()) {
            Rule rule = (Rule)var3.next();
            Rule.Action action = rule.getAppliedAction();
            if (action != null) {
               lastAction = action;
            }
         }

         if (lastAction == Rule.Action.ALLOW) {
            return true;
         } else {
            return false;
         }
      }
   }

   public String getIncompatibilityReason() {
      return this.incompatibilityReason;
   }
}
