package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.tlauncher.ui.loc.Localizable;

public class MediaFileExplorer extends FilteredFileExplorer {
   MediaFileExplorer() {
      this.addExtesion(ImageFileExplorer.IMAGE_EXTENSIONS);
      this.addExtesion(new String[]{"mp4", "flv"});
   }

   protected String getDescription() {
      return Localizable.get("explorer.type.media");
   }

   public static MediaFileExplorer newExplorer() throws Exception {
      return new MediaFileExplorer();
   }
}
