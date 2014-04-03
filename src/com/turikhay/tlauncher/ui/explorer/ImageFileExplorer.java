package com.turikhay.tlauncher.ui.explorer;

public class ImageFileExplorer extends FileExplorer {
   private static final long serialVersionUID = -5906170445865689621L;

   public ImageFileExplorer(String directory) {
      super(directory);
      this.setAccessory(new ImageFilePreview(this));
      this.setFileFilter(new ImageFileFilter());
      this.setFileView(new ImageFileView());
      this.setAcceptAllFileFilterUsed(false);
   }

   public ImageFileExplorer() {
      this((String)null);
   }
}
