package net.minecraft.launcher.updater;

import com.turikhay.tlauncher.repository.Repository;
import com.turikhay.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;

public class LocalVersionList extends StreamVersionList {
   private File baseDirectory;
   private File baseVersionsDir;

   public LocalVersionList(File baseDirectory) throws IOException {
      this.setBaseDirectory(baseDirectory);
   }

   public File getBaseDirectory() {
      return this.baseDirectory;
   }

   public void setBaseDirectory(File directory) throws IOException {
      if (directory == null) {
         throw new IllegalArgumentException("Base directory is NULL!");
      } else {
         FileUtil.createFolder(directory);
         this.log(new Object[]{"Base directory:", directory.getAbsolutePath()});
         this.baseDirectory = directory;
         this.baseVersionsDir = new File(this.baseDirectory, "versions");
      }
   }

   public void refreshVersions() throws IOException {
      this.clearCache();
      File[] files = this.baseVersionsDir.listFiles();
      if (files != null) {
         File[] var5 = files;
         int var4 = files.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            File directory = var5[var3];
            String id = directory.getName();
            File jsonFile = new File(directory, id + ".json");
            if (directory.isDirectory() && jsonFile.isFile()) {
               try {
                  CompleteVersion version = (CompleteVersion)this.gson.fromJson(this.getUrl("versions/" + id + "/" + id + ".json"), CompleteVersion.class);
                  if (version == null) {
                     this.log(new Object[]{"JSON descriptor of version \"" + id + "\" in NULL, it won't be added in list as local."});
                  } else {
                     version.setID(id);
                     version.setSource(Repository.LOCAL_VERSION_REPO);
                     version.setVersionList(this);
                     this.addVersion(version);
                  }
               } catch (Exception var9) {
                  this.log(new Object[]{"Error occurred while parsing local version", id, var9});
               }
            }
         }

      }
   }

   public void saveVersion(CompleteVersion version) throws IOException {
      String text = this.serializeVersion(version);
      File target = new File(this.baseVersionsDir, version.getID() + "/" + version.getID() + ".json");
      FileUtil.writeFile(target, text);
   }

   protected InputStream getInputStream(String uri) throws IOException {
      return new FileInputStream(new File(this.baseDirectory, uri));
   }

   public boolean hasAllFiles(CompleteVersion version, OperatingSystem os) {
      Set files = version.getRequiredFiles(os);
      Iterator var5 = files.iterator();

      File required;
      do {
         if (!var5.hasNext()) {
            return true;
         }

         String file = (String)var5.next();
         required = new File(this.baseDirectory, file);
      } while(required.isFile() && required.length() != 0L);

      return false;
   }
}
