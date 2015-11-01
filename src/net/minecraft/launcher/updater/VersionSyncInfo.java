package net.minecraft.launcher.updater;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;

public class VersionSyncInfo {
   protected Version localVersion;
   protected Version remoteVersion;
   private CompleteVersion completeLocal;
   private CompleteVersion completeRemote;
   private String id;

   public VersionSyncInfo(Version localVersion, Version remoteVersion) {
      if (localVersion == null && remoteVersion == null) {
         throw new NullPointerException("Cannot create sync info from NULLs!");
      } else {
         this.localVersion = localVersion;
         this.remoteVersion = remoteVersion;
         if (localVersion != null && remoteVersion != null) {
            localVersion.setVersionList(remoteVersion.getVersionList());
         }

         if (this.getID() == null) {
            throw new NullPointerException("Cannot create sync info from versions that have NULL IDs");
         }
      }
   }

   public VersionSyncInfo(VersionSyncInfo info) {
      this(info.getLocal(), info.getRemote());
   }

   protected VersionSyncInfo() {
      this.localVersion = null;
      this.remoteVersion = null;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (this.getID() != null && o != null && o instanceof VersionSyncInfo) {
         VersionSyncInfo v = (VersionSyncInfo)o;
         return this.getID().equals(v.getID());
      } else {
         return false;
      }
   }

   public Version getLocal() {
      return this.localVersion;
   }

   public void setLocal(Version version) {
      this.localVersion = version;
      if (version instanceof CompleteVersion) {
         this.completeLocal = (CompleteVersion)version;
      }

   }

   public Version getRemote() {
      return this.remoteVersion;
   }

   public void setRemote(Version version) {
      this.remoteVersion = version;
      if (version instanceof CompleteVersion) {
         this.completeRemote = (CompleteVersion)version;
      }

   }

   public String getID() {
      return this.id != null ? this.id : (this.localVersion != null ? this.localVersion.getID() : (this.remoteVersion != null ? this.remoteVersion.getID() : null));
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

   public Version getAvailableVersion() {
      return this.localVersion != null ? this.localVersion : this.remoteVersion;
   }

   public boolean isInstalled() {
      return this.localVersion != null;
   }

   public boolean hasRemote() {
      return this.remoteVersion != null;
   }

   public boolean isUpToDate() {
      return this.localVersion == null ? false : (this.remoteVersion == null ? true : this.localVersion.getUpdatedTime().compareTo(this.remoteVersion.getUpdatedTime()) >= 0);
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{id='" + this.getID() + "',\nlocal=" + this.localVersion + ",\nremote=" + this.remoteVersion + ", isInstalled=" + this.isInstalled() + ", hasRemote=" + this.hasRemote() + ", isUpToDate=" + this.isUpToDate() + "}";
   }

   public CompleteVersion resolveCompleteVersion(VersionManager manager, boolean latest) throws IOException {
      Version version;
      if (latest) {
         version = this.getLatestVersion();
      } else if (this.isInstalled()) {
         version = this.getLocal();
      } else {
         version = this.getRemote();
      }

      if (version.equals(this.localVersion) && this.completeLocal != null && this.completeLocal.getInheritsFrom() == null) {
         return this.completeLocal;
      } else if (version.equals(this.remoteVersion) && this.completeRemote != null && this.completeRemote.getInheritsFrom() == null) {
         return this.completeRemote;
      } else {
         CompleteVersion complete = version.getVersionList().getCompleteVersion(version).resolve(manager, latest);
         if (version == this.localVersion) {
            this.completeLocal = complete;
         } else if (version == this.remoteVersion) {
            this.completeRemote = complete;
         }

         return complete;
      }
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
         if (version == this.localVersion) {
            this.completeLocal = complete;
         } else if (version == this.remoteVersion) {
            this.completeRemote = complete;
         }

         return complete;
      }
   }

   public CompleteVersion getLatestCompleteVersion() throws IOException {
      return this.getCompleteVersion(true);
   }

   public CompleteVersion getLocalCompleteVersion() {
      return this.completeLocal;
   }

   Set getRequiredDownloadables(OS os, File targetDirectory, boolean force, boolean ely) throws IOException {
      HashSet neededFiles = new HashSet();
      CompleteVersion version = this.getCompleteVersion(force);
      if (ely) {
         version = TLauncher.getInstance().getElyManager().elyficate(version);
      }

      Repository source = this.hasRemote() ? this.remoteVersion.getSource() : Repository.OFFICIAL_VERSION_REPO;
      if (!source.isSelectable()) {
         return neededFiles;
      } else {
         Iterator var9 = version.getRelevantLibraries().iterator();

         while(true) {
            Library library;
            File local1;
            do {
               String file;
               do {
                  if (!var9.hasNext()) {
                     return neededFiles;
                  }

                  library = (Library)var9.next();
                  file = null;
                  if (library.getNatives() != null) {
                     String local = (String)library.getNatives().get(os);
                     if (local != null) {
                        file = library.getArtifactPath(local);
                     }
                  } else {
                     file = library.getArtifactPath();
                  }
               } while(file == null);

               local1 = new File(targetDirectory, "libraries/" + file);
            } while(!force && local1.isFile() && (library.getChecksum() == null || library.getChecksum().equals(FileUtil.getChecksum(local1, "SHA-1"))));

            neededFiles.add(library.getDownloadable(source, local1, os));
         }
      }
   }

   public Set getRequiredDownloadables(File targetDirectory, boolean force, boolean ely) throws IOException {
      return this.getRequiredDownloadables(OS.CURRENT, targetDirectory, force, ely);
   }

   public static VersionSyncInfo createEmpty() {
      return new VersionSyncInfo();
   }
}
