package ru.turikhay.tlauncher.ui.explorer;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;

public class ExtensionFileFilter extends FileFilter {
   private final String extension;
   private final boolean acceptNull;

   public ExtensionFileFilter(String extension, boolean acceptNullExtension) {
      if (extension == null) {
         throw new NullPointerException("Extension is NULL!");
      } else if (extension.isEmpty()) {
         throw new IllegalArgumentException("Extension is empty!");
      } else {
         this.extension = extension;
         this.acceptNull = acceptNullExtension;
      }
   }

   public ExtensionFileFilter(String extension) {
      this(extension, true);
   }

   public String getExtension() {
      return this.extension;
   }

   public boolean acceptsNull() {
      return this.acceptNull;
   }

   public boolean accept(File f) {
      String currentExtension = FileUtil.getExtension(f);
      return this.acceptNull && currentExtension == null ? true : this.extension.equals(currentExtension);
   }

   public String getDescription() {
      return Localizable.get("explorer.extension.format", this.extension.toUpperCase());
   }
}
