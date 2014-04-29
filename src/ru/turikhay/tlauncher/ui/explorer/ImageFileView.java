package ru.turikhay.tlauncher.ui.explorer;

import java.io.File;
import javax.swing.filechooser.FileView;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;

public class ImageFileView extends FileView {
   public String getTypeDescription(File f) {
      String extension = FileUtil.getExtension(f);
      String localized = Localizable.nget("explorer.extension." + extension);
      return localized;
   }
}
