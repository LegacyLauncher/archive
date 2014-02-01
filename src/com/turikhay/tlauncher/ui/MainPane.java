package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.ui.backgrounds.Background;
import com.turikhay.tlauncher.ui.backgrounds.DefaultBackground;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.scenes.PseudoScene;
import java.awt.Component;
import java.awt.Point;

public class MainPane extends ExtendedLayeredPane {
   private static final long serialVersionUID = -8854598755786867602L;
   private final TLauncherFrame rootFrame;
   private Background background;
   private PseudoScene scene;
   private final ConnectionWarning warning;
   private final ProgressBar progress;
   public final DefaultBackground defaultBackground;
   public final DefaultScene defaultScene;
   public final AccountEditorScene accountEditor;

   MainPane(TLauncherFrame frame) {
      this.rootFrame = frame;
      this.background = this.defaultBackground = new DefaultBackground(this);
      this.add(this.defaultBackground);
      this.defaultScene = new DefaultScene(this);
      this.add(this.defaultScene);
      this.accountEditor = new AccountEditorScene(this);
      this.add(this.accountEditor);
      this.progress = new ProgressBar(frame);
      this.add(this.progress);
      this.warning = new ConnectionWarning();
      this.warning.setLocation(10, 10);
      this.add(this.warning);
      this.setScene(this.defaultScene, false);
   }

   public void showBackground() {
      this.background.setShown(true);
   }

   public void hideBackground() {
      this.background.setShown(false);
   }

   public Background getBackgroundPane() {
      return this.background;
   }

   public void setBackgroundPane(Background background) {
      if (background == null) {
         throw new NullPointerException();
      } else if (!this.background.equals(background)) {
         Component[] var5;
         int var4 = (var5 = this.getComponents()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component comp = var5[var3];
            if (!comp.equals(background) && comp instanceof Background) {
               ((Background)comp).setShown(false);
            }
         }

         this.background = background;
         this.background.setShown(true);
      }
   }

   public DefaultBackground getDefaultBackgroundPane() {
      return this.defaultBackground;
   }

   public PseudoScene getScene() {
      return this.scene;
   }

   public void setScene(PseudoScene scene) {
      this.setScene(scene, true);
   }

   public void setScene(PseudoScene scene, boolean animate) {
      if (scene == null) {
         throw new NullPointerException();
      } else if (!scene.equals(this.scene)) {
         Component[] var6;
         int var5 = (var6 = this.getComponents()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Component comp = var6[var4];
            if (!comp.equals(scene) && comp instanceof PseudoScene) {
               ((PseudoScene)comp).setShown(false, animate);
            }
         }

         this.scene = scene;
         this.scene.setShown(true);
      }
   }

   public void openDefaultScene() {
      this.setScene(this.defaultScene);
   }

   public void openAccountEditor() {
      this.setScene(this.accountEditor);
   }

   public TLauncherFrame getRootFrame() {
      return this.rootFrame;
   }

   public void onResize() {
      super.onResize();
      this.progress.setLocation(0, this.getHeight() - this.progress.getWidth());
   }

   public Point getLocationOf(Component comp) {
      Point compLocation = comp.getLocationOnScreen();
      Point paneLocation = this.getLocationOnScreen();
      return new Point(compLocation.x - paneLocation.x, compLocation.y - paneLocation.y);
   }
}
