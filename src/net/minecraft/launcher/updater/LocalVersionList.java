package net.minecraft.launcher.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFrame;
import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

public class LocalVersionList extends StreamVersionList {
   private FileExplorer explorer;
   private JFrame parent;
   private File baseDirectory;
   private File baseVersionsDir;

   public LocalVersionList() throws IOException {
      this.setBaseDirectory(MinecraftUtil.getWorkingDirectory());
   }

   public File getBaseDirectory() {
      return this.baseDirectory;
   }

   public File getVersionsDirectory() {
      return this.baseVersionsDir;
   }

   public void setBaseDirectory(File directory) throws IOException {
      if (directory == null) {
         throw new IllegalArgumentException("Base directory is NULL!");
      } else if (!directory.isDirectory()) {
         throw new IOException("Directory is not yet created!");
      } else if (!directory.canWrite()) {
         throw new IOException("Directory is not accessible!");
      } else {
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
                  CompleteVersion ex = (CompleteVersion)this.gson.fromJson(this.getUrl("versions/" + id + "/" + id + ".json"), CompleteVersion.class);
                  if (ex == null) {
                     this.log(new Object[]{"JSON descriptor of version \"" + id + "\" in NULL, it won't be added in list as local."});
                  } else {
                     ex.setID(id);
                     ex.setSource(Repository.LOCAL_VERSION_REPO);
                     ex.setVersionList(this);
                     this.addVersion(ex);
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

   public void deleteVersion(String id, boolean deleteLibraries) throws IOException {
      CompleteVersion version = this.getCompleteVersion(id);
      if (version == null) {
         throw new IllegalArgumentException("Version is not installed!");
      } else {
         File dir = new File(this.baseVersionsDir, id + '/');
         if (!dir.isDirectory()) {
            throw new IOException("Cannot find directory: " + dir.getAbsolutePath());
         } else {
            FileUtil.deleteDirectory(dir);
            if (deleteLibraries) {
               Iterator var6 = version.getClassPath(this.baseDirectory).iterator();

               while(var6.hasNext()) {
                  File nativeLib = (File)var6.next();
                  FileUtil.deleteFile(nativeLib);
               }

               var6 = version.getNatives().iterator();

               while(var6.hasNext()) {
                  String nativeLib1 = (String)var6.next();
                  FileUtil.deleteFile(new File(this.baseDirectory, nativeLib1));
               }
            }

         }
      }
   }

   protected InputStream getInputStream(String uri) throws IOException {
      return new FileInputStream(new File(this.baseDirectory, uri));
   }

   public boolean hasAllFiles(CompleteVersion version, OS os) {
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
