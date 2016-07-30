package ru.turikhay.tlauncher.ui.explorer;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

public class FileExplorer extends JFileChooser {
   public void setCurrentDirectory(File dir) {
      if (dir == null) {
         dir = this.getFileSystemView().getDefaultDirectory();
      }

      super.setCurrentDirectory(dir);
   }

   public int showDialog(Component parent) {
      return this.showDialog(parent, UIManager.getString("FileChooser.directoryOpenButtonText"));
   }

   public File[] getSelectedFiles() {
      File[] selectedFiles = super.getSelectedFiles();
      if (selectedFiles.length > 0) {
         return selectedFiles;
      } else {
         File selectedFile = super.getSelectedFile();
         return selectedFile == null ? null : new File[]{selectedFile};
      }
   }

   public static FileExplorer newExplorer() throws Exception {
      try {
         return new FileExplorer();
      } catch (Throwable var1) {
         throw new Exception("couldn't create explorer");
      }
   }
}
