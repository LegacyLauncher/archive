package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.ui.background.BackgroundHolder;
import com.turikhay.tlauncher.ui.progress.LaunchProgress;
import com.turikhay.tlauncher.ui.progress.ProgressBar;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.scenes.PseudoScene;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import java.awt.Component;
import java.awt.Point;

public class MainPane extends ExtendedLayeredPane {
   private static final long serialVersionUID = -8854598755786867602L;
   private final TLauncherFrame rootFrame;
   private PseudoScene scene;
   public final BackgroundHolder background;
   public final LaunchProgress progress;
   public final DefaultScene defaultScene;
   public final AccountEditorScene accountEditor;
   public final ConnectionWarning warning;

   MainPane(TLauncherFrame frame) {
      super((Component)null);
      this.rootFrame = frame;
      this.background = new BackgroundHolder(this);
      this.background.setBackground(this.background.SLIDE_BACKGROUND, false);
      this.add(this.background);
      this.defaultScene = new DefaultScene(this);
      this.add(this.defaultScene);
      this.accountEditor = new AccountEditorScene(this);
      this.add(this.accountEditor);
      this.progress = new LaunchProgress(frame);
      this.add(this.progress);
      this.warning = new ConnectionWarning();
      this.warning.setLocation(10, 10);
      this.add(this.warning);
      this.setScene(this.defaultScene, false);
   }

   public PseudoScene getScene() {
      return this.scene;
   }

   void setScene(PseudoScene scene) {
      this.setScene(scene, true);
   }

   void setScene(PseudoScene scene, boolean animate) {
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

   public LaunchProgress getProgress() {
      return this.progress;
   }

   public void onResize() {
      super.onResize();
      this.progress.setBounds(0, this.getHeight() - ProgressBar.DEFAULT_HEIGHT + 1, this.getWidth(), ProgressBar.DEFAULT_HEIGHT);
   }

   public Point getLocationOf(Component comp) {
      Point compLocation = comp.getLocationOnScreen();
      Point paneLocation = this.getLocationOnScreen();
      return new Point(compLocation.x - paneLocation.x, compLocation.y - paneLocation.y);
   }
}
