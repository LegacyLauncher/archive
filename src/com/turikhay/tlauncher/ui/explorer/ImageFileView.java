package com.turikhay.tlauncher.ui.explorer;

import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.FileUtil;
import java.io.File;
import javax.swing.filechooser.FileView;

public class ImageFileView extends FileView {
   public String getTypeDescription(File f) {
      String extension = FileUtil.getExtension(f);
      String localized = Localizable.nget("explorer.extension." + extension);
      return localized;
   }
}
