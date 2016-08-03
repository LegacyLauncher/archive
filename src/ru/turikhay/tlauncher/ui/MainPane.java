package ru.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.background.BackgroundManager;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.progress.LaunchProgress;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.scenes.PseudoScene;
import ru.turikhay.tlauncher.ui.scenes.VersionManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class MainPane extends ExtendedLayeredPane {
   private final TLauncherFrame rootFrame;
   private final boolean repaintEveryTime;
   private PseudoScene scene;
   public final BackgroundManager background;
   public final LaunchProgress progress;
   public final DefaultScene defaultScene;
   public final AccountEditorScene accountEditor;
   public final VersionManagerScene versionManager;
   public final SideNotifier notifier;
   public final MainPane.RevertFontSize revertFont;

   MainPane(TLauncherFrame frame) {
      super((JComponent)null);
      this.rootFrame = frame;
      this.repaintEveryTime = OS.LINUX.isCurrent();
      this.log("Creating background...");
      this.background = new BackgroundManager(this);
      this.add(this.background);
      this.notifier = new SideNotifier();
      this.notifier.setSize(SwingUtil.magnify(new Dimension(32, 32)));
      this.add(this.notifier);
      this.log("Init Default scene...");
      this.defaultScene = new DefaultScene(this);
      this.add(this.defaultScene);
      this.log("Init Account editor scene...");
      this.accountEditor = new AccountEditorScene(this);
      this.add(this.accountEditor);
      this.log("Init Version manager scene...");
      this.versionManager = new VersionManagerScene(this);
      this.add(this.versionManager);
      this.progress = new LaunchProgress(frame);
      this.add(this.progress);
      this.revertFont = new MainPane.RevertFontSize();
      if (this.revertFont.shouldShow()) {
         this.add(this.revertFont);
      }

      this.setScene(this.defaultScene, false);
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            MainPane.this.onResize();
         }
      });
   }

   public void setScene(PseudoScene scene) {
      this.setScene(scene, true);
   }

   public void setScene(PseudoScene newscene, boolean animate) {
      if (newscene == null) {
         throw new NullPointerException();
      } else {
         if (!newscene.equals(this.scene)) {
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
            if (this.repaintEveryTime) {
               this.repaint();
            }
         }

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
      this.revertFont.setBounds(0, 0, this.getWidth(), this.getFontMetrics(this.revertFont.revertButton.getFont()).getHeight() * 3);
   }

   private void log(String... o) {
      U.log("[MainPane]", o);
   }

   public class RevertFontSize extends ExtendedPanel implements LocalizableComponent {
      private final LocalizableButton revertButton;
      private final LocalizableButton closeButton;
      private final float oldSize;
      private final int oldSizeInt;

      private RevertFontSize() {
         float size = (float)MainPane.this.rootFrame.getConfiguration().getInteger("gui.font.old");
         if (size < 12.0F || size > 18.0F) {
            size = 18.0F;
         }

         this.oldSize = size;
         this.oldSizeInt = (int)size;
         this.revertButton = new LocalizableButton("revert.font.approve");
         this.revertButton.setFont(this.revertButton.getFont().deriveFont(size));
         this.revertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               MainPane.this.defaultScene.settingsForm.font.setValue(RevertFontSize.this.oldSizeInt);
               MainPane.this.defaultScene.settingsForm.saveValues();
               Alert.showLocMessage("revert.font.approved");
               RevertFontSize.this.closeButton.doClick();
            }
         });
         this.closeButton = new LocalizableButton("revert.font.close");
         this.closeButton.setToolTipText("revert.font.close.hint");
         this.closeButton.setFont(this.closeButton.getFont().deriveFont(1, size));
         this.closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               MainPane.this.rootFrame.getConfiguration().set("gui.font.old", MainPane.this.rootFrame.getConfiguration().getInteger("gui.font"));
               MainPane.this.remove(RevertFontSize.this);
               MainPane.this.repaint();
            }
         });
         this.add(this.revertButton, this.closeButton);
         this.updateLocale();
      }

      public boolean shouldShow() {
         return TLauncherFrame.getFontSize() != this.oldSize;
      }

      public void updateLocale() {
         Localizable.updateContainer(this);
      }

      // $FF: synthetic method
      RevertFontSize(Object x1) {
         this();
      }
   }
}
