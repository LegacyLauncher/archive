package ru.turikhay.tlauncher.ui.background;

import javax.swing.JComponent;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.background.fx.MediaFxBackground;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public final class BackgroundManager extends ExtendedLayeredPane {
   final Worker worker = new Worker(this);
   final Cover cover = new Cover();
   private final ImageBackground imageBackground;
   private final FXWrapper mediaFxBackground;
   private IBackground background;

   public BackgroundManager(MainPane pane) {
      super(pane);
      this.add(this.cover, Integer.MAX_VALUE);
      this.imageBackground = new ImageBackground();
      if (OS.JAVA_VERSION.getDouble() >= 1.8D) {
         this.mediaFxBackground = new FXWrapper(MediaFxBackground.class);
      } else {
         this.mediaFxBackground = null;
      }

   }

   public FXWrapper getMediaFxBackground() {
      return this.mediaFxBackground;
   }

   void setBackground(IBackground background) {
      if (this.background != background) {
         if (this.background != null) {
            this.remove((JComponent)this.background);
         }

         if (background != null) {
            this.add((JComponent)background, 1);
         }

         this.background = background;
         this.onResize();
      }
   }

   public void startBackground() {
      if (this.background != null) {
         this.background.startBackground();
      }

   }

   public void pauseBackground() {
      if (this.background != null) {
         this.background.pauseBackground();
      }

   }

   public void loadBackground() {
      String path = TLauncher.getInstance().getSettings().get("gui.background");
      if (path == null || this.mediaFxBackground == null || !path.endsWith(".mp4") && !path.endsWith(".flv")) {
         this.worker.setBackground(this.imageBackground, path);
      } else {
         this.worker.setBackground(this.mediaFxBackground, path);
      }

   }

   public void onResize() {
      U.log("background manager resized", this.getSize(), this.getParent().getSize());
      super.onResize();
   }
}
