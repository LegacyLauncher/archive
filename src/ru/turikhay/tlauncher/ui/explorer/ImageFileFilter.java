package ru.turikhay.tlauncher.ui.explorer;

import java.io.File;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileFilter;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;

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
