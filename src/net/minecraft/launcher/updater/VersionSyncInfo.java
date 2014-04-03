package net.minecraft.launcher.updater;

import com.turikhay.tlauncher.repository.Repository;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Version;

public class VersionSyncInfo {
   protected final Version localVersion;
   protected final Version remoteVersion;
   private CompleteVersion completeLocal;
   private CompleteVersion completeRemote;
   private String id;

   public VersionSyncInfo(Version localVersion, Version remoteVersion) {
      if (localVersion == null && remoteVersion == null) {
         throw new NullPointerException("Cannot create sync info from NULLs!");
      } else {
         this.localVersion = localVersion;
         this.remoteVersion = remoteVersion;
         if (this.getID() == null) {
            throw new NullPointerException("Cannot create sync info from versions that have NULL IDs");
         }
      }
   }

   public VersionSyncInfo(VersionSyncInfo info) {
      this(info.getLocal(), info.getRemote());
   }

   private VersionSyncInfo() {
      this.localVersion = null;
      this.remoteVersion = null;
   }

   public Object clone() {
      return new VersionSyncInfo(this);
   }

   public Version getLocal() {
      return this.localVersion;
   }

   public Version getRemote() {
      return this.remoteVersion;
   }

   public String getID() {
      if (this.id != null) {
         return this.id;
      } else if (this.localVersion != null) {
         return this.localVersion.getID();
      } else {
         return this.remoteVersion != null ? this.remoteVersion.getID() : null;
      }
   }

   public void setID(String id) {
      if (id != null && id.isEmpty()) {
         throw new IllegalArgumentException("ID cannot be empty!");
      } else {
         this.id = id;
      }
   }

   public Version getLatestVersion() {
      return this.remoteVersion != null ? this.remoteVersion : this.localVersion;
   }

   public boolean isInstalled() {
      return this.localVersion != null;
   }

   public boolean hasRemote() {
      return this.remoteVersion != null;
   }

   public boolean isUpToDate() {
      if (this.localVersion == null) {
         return false;
      } else if (this.remoteVersion == null) {
         return true;
      } else {
         return this.localVersion.getUpdatedTime().compareTo(this.remoteVersion.getUpdatedTime()) >= 0;
      }
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{id='" + this.getID() + "',\nlocal=" + this.localVersion + ",\nremote=" + this.remoteVersion + ", isInstalled=" + this.isInstalled() + ", hasRemote=" + this.hasRemote() + ", isUpToDate=" + this.isUpToDate() + "}";
   }

   public CompleteVersion getCompleteVersion(boolean latest) throws IOException {
      Version version;
      if (latest) {
         version = this.getLatestVersion();
      } else if (this.isInstalled()) {
         version = this.getLocal();
      } else {
         version = this.getRemote();
      }

      if (version.equals(this.localVersion) && this.completeLocal != null) {
         return this.completeLocal;
      } else if (version.equals(this.remoteVersion) && this.completeRemote != null) {
         return this.completeRemote;
      } else {
         CompleteVersion complete = version.getVersionList().getCompleteVersion(version);
         if (version.equals(this.localVersion)) {
            this.completeLocal = complete;
         } else if (version.equals(this.remoteVersion)) {
            this.completeRemote = complete;
         }

         return complete;
      }
   }

   public CompleteVersion getLatestCompleteVersion() throws IOException {
      return this.getCompleteVersion(true);
   }

   Set getRequiredDownloadables(OperatingSystem os, File targetDirectory, boolean force) throws IOException {
      Set neededFiles = new HashSet();
      CompleteVersion version = this.getCompleteVersion(force);
      Repository source = this.hasRemote() ? this.remoteVersion.getSource() : Repository.OFFICIAL_VERSION_REPO;
      if (!source.isSelectable()) {
         return neededFiles;
      } else {
         Iterator var8 = version.getRelevantLibraries().iterator();

         while(true) {
            Library library;
            File local;
            do {
               String file;
               do {
                  if (!var8.hasNext()) {
                     return neededFiles;
                  }

                  library = (Library)var8.next();
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

               local = new File(targetDirectory, "libraries/" + file);
            } while(!force && local.isFile() && local.length() > 0L);

            neededFiles.add(library.getDownloadable(source, local, os));
         }
      }
   }

   public Set getRequiredDownloadables(File targetDirectory, boolean force) throws IOException {
      return this.getRequiredDownloadables(OperatingSystem.getCurrentPlatform(), targetDirectory, force);
   }

   public static VersionSyncInfo createEmpty() {
      return new VersionSyncInfo();
   }
}
