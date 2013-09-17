package net.minecraft.launcher_.updater;

import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;
import net.minecraft.launcher_.versions.VersionSource;

public class LocalVersionList extends FileBasedVersionList {
   private final File baseDirectory;
   private final File baseVersionsDir;

   public LocalVersionList(File baseDirectory) {
      if (baseDirectory != null && baseDirectory.isDirectory()) {
         this.baseDirectory = baseDirectory;
         this.baseVersionsDir = new File(this.baseDirectory, "versions");
         if (!this.baseVersionsDir.isDirectory()) {
            this.baseVersionsDir.mkdirs();
         }

      } else {
         throw new IllegalArgumentException("Base directory is not a folder!");
      }
   }

   protected InputStream getFileInputStream(String uri) throws FileNotFoundException {
      return new FileInputStream(new File(this.baseDirectory, uri));
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
            if (directory.isDirectory() && jsonFile.exists()) {
               try {
                  CompleteVersion version = (CompleteVersion)this.gson.fromJson(this.getUrl("versions/" + id + "/" + id + ".json"), CompleteVersion.class);
                  version.setId(id);
                  this.addVersion(version);
               } catch (JsonSyntaxException var9) {
                  throw new JsonSyntaxException("Loading file: " + jsonFile.toString(), var9);
               }
            }
         }

         Iterator var11 = this.getVersions().iterator();

         while(true) {
            Version version;
            ReleaseType type;
            do {
               if (!var11.hasNext()) {
                  return;
               }

               version = (Version)var11.next();
               type = version.getType();
            } while(this.getLatestVersion(type) != null && !this.getLatestVersion(type).getUpdatedTime().before(version.getUpdatedTime()));

            this.setLatestVersion(version);
         }
      }
   }

   public void saveVersionList() throws IOException {
      String text = this.serializeVersionList();
      PrintWriter writer = new PrintWriter(new File(this.baseVersionsDir, "versions.json"));
      writer.print(text);
      writer.close();
   }

   public void saveVersion_(CompleteVersion version) throws IOException {
      String text = this.serializeVersion(version);
      File target = new File(this.baseVersionsDir, version.getId() + "/" + version.getId() + ".json");
      FileUtil.saveFile(target, text);
   }

   public void saveVersion(CompleteVersion version) {
      try {
         this.saveVersion_(version);
      } catch (IOException var3) {
         var3.printStackTrace();
      }

   }

   public File getBaseDirectory() {
      return this.baseDirectory;
   }

   public boolean hasAllFiles(CompleteVersion version, OperatingSystem os) {
      Set files = version.getRequiredFiles(os);
      Iterator var5 = files.iterator();

      while(var5.hasNext()) {
         String file = (String)var5.next();
         if (!(new File(this.baseDirectory, file)).isFile()) {
            return false;
         }
      }

      return true;
   }

   public VersionSource getRepositoryType() {
      return VersionSource.LOCAL;
   }
}
