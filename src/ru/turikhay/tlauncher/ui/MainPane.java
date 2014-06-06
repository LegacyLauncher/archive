package ru.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.progress.LaunchProgress;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.scenes.PseudoScene;
import ru.turikhay.tlauncher.ui.scenes.VersionManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class MainPane extends ExtendedLayeredPane {
   private static final long serialVersionUID = -8854598755786867602L;
   private final TLauncherFrame rootFrame;
   private PseudoScene scene;
   public final BackgroundHolder background;
   public final LaunchProgress progress;
   public final DefaultScene defaultScene;
   public final AccountEditorScene accountEditor;
   public final VersionManagerScene versionManager;
   final ServicePanel service;
   public final LeftSideNotifier warning;

   MainPane(TLauncherFrame frame) {
      super((JComponent)null);
      this.rootFrame = frame;
      this.background = new BackgroundHolder(this);
      this.background.setBackground(this.background.SLIDE_BACKGROUND, false);
      this.add(this.background);
      this.service = new ServicePanel(this);
      this.defaultScene = new DefaultScene(this);
      this.add(this.defaultScene);
      this.accountEditor = new AccountEditorScene(this);
      this.add(this.accountEditor);
      this.versionManager = new VersionManagerScene(this);
      this.add(this.versionManager);
      this.progress = new LaunchProgress(frame);
      this.add(this.progress);
      this.warning = new LeftSideNotifier();
      this.warning.setLocation(10, 10);
      this.add(this.warning);
      this.setScene(this.defaultScene, false);
   }

   public PseudoScene getScene() {
      return this.scene;
   }

   public void setScene(PseudoScene scene) {
      this.setScene(scene, true);
   }

   public void setScene(PseudoScene newscene, boolean animate) {
      if (newscene == null) {
         throw new NullPointerException();
      } else if (!newscene.equals(this.scene)) {
         Component[] var6;
         int var5 = (var6 = this.getComponents()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Component comp = var6[var4];
            if (!comp.equals(newscene) && comp instanceof PseudoScene) {
               ((PseudoScene)comp).setShown(false, animate);
            }
         }

         this.scene = newscene;
         this.scene.setShown(true);
      }
   }

   public void openDefaultScene() {
      this.setScene(this.defaultScene);
   }

   public void openAccountEditor() {
      this.setScene(this.accountEditor);
   }

   public void openVersionManager() {
      this.setScene(this.versionManager);
   }

   public TLauncherFrame getRootFrame() {
      return this.rootFrame;
   }

   public LaunchProgress getProgress() {
      return this.progress;
   }

   public void onResize() {
      this.progress.setBounds(0, this.getHeight() - ProgressBar.DEFAULT_HEIGHT + 1, this.getWidth(), ProgressBar.DEFAULT_HEIGHT);
   }

   public Point getLocationOf(Component comp) {
      Point compLocation = comp.getLocationOnScreen();
      Point paneLocation = this.getLocationOnScreen();
      return new Point(compLocation.x - paneLocation.x, compLocation.y - paneLocation.y);
   }
}
