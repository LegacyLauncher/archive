package com.turikhay.tlauncher.ui.explorer;

import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.FileUtil;
import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;

public class ImageFileFilter extends FileFilter {
   public static final Pattern extensionPattern = Pattern.compile("^(?:jp(?:e|)g|png)$", 2);

   public boolean accept(File f) {
      String extension = FileUtil.getExtension(f);
      return extension == null ? true : extensionPattern.matcher(extension).matches();
   }

   public String getDescription() {
      return Localizable.get("explorer.type.image");
   }
}
