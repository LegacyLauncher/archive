package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.tlauncher.ui.loc.Localizable;

public class ImageFileExplorer extends FilteredFileExplorer {
   static final String[] IMAGE_EXTENSIONS = new String[]{"png", "jpg", "jpeg"};

   ImageFileExplorer() {
      this.addExtesion((String[])IMAGE_EXTENSIONS);
      this.setAccessory(new ImageFilePreview(this));
      this.setAcceptAllFileFilterUsed(false);
   }

   protected String getDescription() {
      return Localizable.get("explorer.type.image");
   }

   public static ImageFileExplorer newExplorer() throws Exception {
      return new ImageFileExplorer();
   }
}
