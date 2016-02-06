package ru.turikhay.tlauncher.ui.explorer;

public class ImageFileExplorer extends FileExplorer {
   protected ImageFileExplorer() {
      this.setAccessory(new ImageFilePreview(this));
      this.setFileFilter(new ImageFileFilter());
      this.setFileView(new ImageFileView());
      this.setAcceptAllFileFilterUsed(false);
   }

   public static ImageFileExplorer newExplorer() throws InternalError {
      try {
         return new ImageFileExplorer();
      } catch (Throwable var1) {
         throw new InternalError("couldn't create explorer");
      }
   }
}
