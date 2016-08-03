package ru.turikhay.tlauncher.ui.explorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public abstract class FilteredFileExplorer extends FileExplorer {
   private final List extensionList = new ArrayList();

   protected FilteredFileExplorer() {
      this.setFileFilter(new FileFilter() {
         public boolean accept(File f) {
            String extension = FileUtil.getExtension(f);
            return extension == null || FilteredFileExplorer.this.extensionList.contains(extension);
         }

         public String getDescription() {
            return FilteredFileExplorer.this.getDescription();
         }
      });
      this.setAcceptAllFileFilterUsed(false);
   }

   protected abstract String getDescription();

   protected void addExtesion(String... ext) {
      Collections.addAll(this.extensionList, U.requireNotContainNull((Object[])ext, "ext"));
   }
}
