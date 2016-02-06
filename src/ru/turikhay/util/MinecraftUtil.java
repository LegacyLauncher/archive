package ru.turikhay.util;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;

public class MinecraftUtil {
   private static FileExplorer explorer;
   private static JFrame parent;

   public static File getWorkingDirectory() {
      File defaultDirectory = getDefaultWorkingDirectory();
      if (TLauncher.getInstance() == null) {
         return defaultDirectory;
      } else {
         String path = TLauncher.getInstance().getSettings().get("minecraft.gamedir");
         if (path == null) {
            return defaultDirectory;
         } else {
            File directory = new File(path);

            do {
               try {
                  FileUtil.createFolder(directory);
               } catch (IOException var7) {
                  Alert.showLocError("version.dir.noaccess", directory);
                  File preferred = new File("/");
                  if (!directory.equals(defaultDirectory)) {
                     try {
                        FileUtil.createFolder(defaultDirectory);
                        preferred = defaultDirectory;
                     } catch (IOException var6) {
                        U.log("Can't even create default folder. Am I in Hell? TELL ME THE TRUTH!", var6);
                     }
                  }

                  directory = showExplorer(preferred);
               }

               if (directory == null) {
                  Alert.showLocWarning("version.dir.exit");
                  TLauncher.kill();
                  return null;
               }
            } while(!directory.isDirectory() || !directory.canWrite());

            return directory;
         }
      }
   }

   private static File showExplorer(File preferred) {
      if (explorer == null) {
         parent = new JFrame();

         try {
            explorer = FileExplorer.newExplorer();
            explorer.setSelectedFile(preferred);
            explorer.setFileSelectionMode(1);
         } catch (InternalError var3) {
            String answer = Alert.showLocInputQuestion("version.dir.noexplorer");
            if (answer == null) {
               return null;
            }

            return new File(answer);
         }
      }

      return explorer.showDialog(parent) != 0 ? null : explorer.getSelectedFile();
   }

   public static File getSystemRelatedFile(String path) {
      String userHome = System.getProperty("user.home", ".");
      File file;
      switch(OS.CURRENT) {
      case LINUX:
      case SOLARIS:
         file = new File(userHome, path);
         break;
      case WINDOWS:
         String applicationData = System.getenv("APPDATA");
         String folder = applicationData != null ? applicationData : userHome;
         file = new File(folder, path);
         break;
      case OSX:
         file = new File(userHome, "Library/Application Support/" + path);
         break;
      default:
         file = new File(userHome, path);
      }

      return file;
   }

   public static File getSystemRelatedDirectory(String path) {
      if (!OS.is(OS.OSX, OS.UNKNOWN)) {
         path = '.' + path;
      }

      return getSystemRelatedFile(path);
   }

   public static File getDefaultWorkingDirectory() {
      return getSystemRelatedDirectory(TLauncher.getFolder());
   }
}
