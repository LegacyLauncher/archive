package ru.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.UIManager;
import ru.turikhay.tlauncher.Bootstrapper;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.swing.Dragger;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class TLauncherFrame extends JFrame {
   public static final Dimension minSize = new Dimension(530, 530);
   public static final Dimension maxSize = new Dimension(1920, 1080);
   public static final float fontSize;
   private final TLauncherFrame instance = this;
   private final TLauncher tlauncher;
   private final Configuration settings;
   private final LangConfiguration lang;
   private final int[] windowSize;
   private final Point maxPoint;
   public final MainPane mp;

   static {
      fontSize = OS.WINDOWS.isCurrent() ? 12.0F : 14.0F;
   }

   public TLauncherFrame(TLauncher t) {
      this.tlauncher = t;
      this.settings = t.getSettings();
      this.lang = t.getLang();
      this.windowSize = this.settings.getLauncherWindowSize();
      this.maxPoint = new Point();
      SwingUtil.initFontSize((int)fontSize);
      SwingUtil.setFavicons(this);
      this.setUILocale();
      this.setWindowSize();
      this.setWindowTitle();
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            TLauncherFrame.this.instance.setVisible(false);
            TLauncher.kill();
         }
      });
      this.setDefaultCloseOperation(3);
      this.addComponentListener(new ExtendedComponentAdapter(this) {
         public void onComponentResized(ComponentEvent e) {
            TLauncherFrame.this.updateMaxPoint();
            Dragger.update();
            boolean lock = TLauncherFrame.this.getExtendedState() != 0;
            Blocker.setBlocked(TLauncherFrame.this.mp.defaultScene.settingsForm.launcherResolution, "extended", lock);
            if (!lock) {
               IntegerArray arr = new IntegerArray(new int[]{TLauncherFrame.this.getWidth(), TLauncherFrame.this.getHeight()});
               TLauncherFrame.this.mp.defaultScene.settingsForm.launcherResolution.setValue(arr);
               TLauncherFrame.this.settings.set("gui.size", arr);
            }
         }

         public void componentShown(ComponentEvent e) {
            TLauncherFrame.this.instance.validate();
            TLauncherFrame.this.instance.repaint();
            TLauncherFrame.this.instance.toFront();
            TLauncherFrame.this.mp.background.startBackground();
         }

         public void componentHidden(ComponentEvent e) {
            TLauncherFrame.this.mp.background.suspendBackground();
         }
      });
      this.addWindowStateListener(new WindowStateListener() {
         public void windowStateChanged(WindowEvent e) {
            int newState = TLauncherFrame.getExtendedStateFor(e.getNewState());
            if (newState != -1) {
               TLauncherFrame.this.settings.set("gui.window", newState);
            }
         }
      });
      U.setLoadingStep(Bootstrapper.LoadingStep.PREPARING_MAINPANE);
      this.mp = new MainPane(this);
      this.add(this.mp);
      U.setLoadingStep(Bootstrapper.LoadingStep.POSTINIT_GUI);
      log("Packing main frame...");
      this.pack();
      log("Resizing main pane...");
      this.mp.onResize();
      this.mp.background.startBackground();
      this.updateMaxPoint();
      Dragger.ready(this.settings, this.maxPoint);
      this.setVisible(true);
      int windowState = getExtendedStateFor(this.settings.getInteger("gui.window"));
      if (windowState == 0) {
         this.setLocationRelativeTo((Component)null);
      } else {
         this.setExtendedState(windowState);
      }

      if (this.settings.isFirstRun()) {
         Alert.showLocAsyncWarning("firstrun");
      }

   }

   public TLauncher getLauncher() {
      return this.tlauncher;
   }

   public Point getMaxPoint() {
      return this.maxPoint;
   }

   public Configuration getConfiguration() {
      return this.settings;
   }

   public void updateLocales() {
      try {
         this.tlauncher.reloadLocale();
      } catch (Exception var2) {
         log("Cannot reload settings!", var2);
         return;
      }

      Console.updateLocale();
      LocalizableMenuItem.updateLocales();
      this.setWindowTitle();
      this.setUILocale();
      Localizable.updateContainer(this);
   }

   public void setWindowTitle() {
      String translator = this.lang.nget("translator");
      String copyright = "(by " + TLauncher.getDeveloper() + (translator != null ? ", translated by " + translator : "") + ")";
      String brand = TLauncher.getBrand() + " " + TLauncher.getVersion() + (TLauncher.isBeta() ? " BETA" : "");
      this.setTitle("TLauncher " + brand + " " + copyright);
   }

   private void setWindowSize() {
      int width = this.windowSize[0] > maxSize.width ? maxSize.width : this.windowSize[0];
      int height = this.windowSize[1] > maxSize.height ? maxSize.height : this.windowSize[1];
      Dimension curSize = new Dimension(width, height);
      this.setMinimumSize(minSize);
      this.setPreferredSize(curSize);
   }

   private void setUILocale() {
      UIManager.put("OptionPane.yesButtonText", this.lang.nget("ui.yes"));
      UIManager.put("OptionPane.noButtonText", this.lang.nget("ui.no"));
      UIManager.put("OptionPane.cancelButtonText", this.lang.nget("ui.cancel"));
      UIManager.put("FileChooser.acceptAllFileFilterText", this.lang.nget("explorer.extension.all"));
      UIManager.put("FileChooser.lookInLabelText", this.lang.nget("explorer.lookin"));
      UIManager.put("FileChooser.saveInLabelText", this.lang.nget("explorer.lookin"));
      UIManager.put("FileChooser.fileNameLabelText", this.lang.nget("explorer.input.filename"));
      UIManager.put("FileChooser.folderNameLabelText", this.lang.nget("explorer.input.foldername"));
      UIManager.put("FileChooser.filesOfTypeLabelText", this.lang.nget("explorer.input.type"));
      UIManager.put("FileChooser.upFolderToolTipText", this.lang.nget("explorer.button.up.tip"));
      UIManager.put("FileChooser.upFolderAccessibleName", this.lang.nget("explorer.button.up"));
      UIManager.put("FileChooser.newFolderToolTipText", this.lang.nget("explorer.button.newfolder.tip"));
      UIManager.put("FileChooser.newFolderAccessibleName", this.lang.nget("explorer.button.newfolder"));
      UIManager.put("FileChooser.newFolderButtonToolTipText", this.lang.nget("explorer.button.newfolder.tip"));
      UIManager.put("FileChooser.newFolderButtonText", this.lang.nget("explorer.button.newfolder"));
      UIManager.put("FileChooser.other.newFolder", this.lang.nget("explorer.button.newfolder.name"));
      UIManager.put("FileChooser.other.newFolder.subsequent", this.lang.nget("explorer.button.newfolder.name"));
      UIManager.put("FileChooser.win32.newFolder", this.lang.nget("explorer.button.newfolder.name"));
      UIManager.put("FileChooser.win32.newFolder.subsequent", this.lang.nget("explorer.button.newfolder.name"));
      UIManager.put("FileChooser.homeFolderToolTipText", this.lang.nget("explorer.button.home.tip"));
      UIManager.put("FileChooser.homeFolderAccessibleName", this.lang.nget("explorer.button.home"));
      UIManager.put("FileChooser.listViewButtonToolTipText", this.lang.nget("explorer.button.list.tip"));
      UIManager.put("FileChooser.listViewButtonAccessibleName", this.lang.nget("explorer.button.list"));
      UIManager.put("FileChooser.detailsViewButtonToolTipText", this.lang.nget("explorer.button.details.tip"));
      UIManager.put("FileChooser.detailsViewButtonAccessibleName", this.lang.nget("explorer.button.details"));
      UIManager.put("FileChooser.viewMenuButtonToolTipText", this.lang.nget("explorer.button.view.tip"));
      UIManager.put("FileChooser.viewMenuButtonAccessibleName", this.lang.nget("explorer.button.view"));
      UIManager.put("FileChooser.newFolderErrorText", this.lang.nget("explorer.error.newfolder"));
      UIManager.put("FileChooser.newFolderErrorSeparator", ": ");
      UIManager.put("FileChooser.newFolderParentDoesntExistTitleText", this.lang.nget("explorer.error.newfolder-nopath"));
      UIManager.put("FileChooser.newFolderParentDoesntExistText", this.lang.nget("explorer.error.newfolder-nopath"));
      UIManager.put("FileChooser.fileDescriptionText", this.lang.nget("explorer.details.file"));
      UIManager.put("FileChooser.directoryDescriptionText", this.lang.nget("explorer.details.dir"));
      UIManager.put("FileChooser.saveButtonText", this.lang.nget("explorer.button.save"));
      UIManager.put("FileChooser.openButtonText", this.lang.nget("explorer.button.open"));
      UIManager.put("FileChooser.saveDialogTitleText", this.lang.nget("explorer.title.save"));
      UIManager.put("FileChooser.openDialogTitleText", this.lang.nget("explorer.title.open"));
      UIManager.put("FileChooser.cancelButtonText", this.lang.nget("explorer.button.cancel"));
      UIManager.put("FileChooser.updateButtonText", this.lang.nget("explorer.button.update"));
      UIManager.put("FileChooser.helpButtonText", this.lang.nget("explorer.button.help"));
      UIManager.put("FileChooser.directoryOpenButtonText", this.lang.nget("explorer.button.open-dir"));
      UIManager.put("FileChooser.saveButtonToolTipText", this.lang.nget("explorer.title.save.tip"));
      UIManager.put("FileChooser.openButtonToolTipText", this.lang.nget("explorer.title.open.tip"));
      UIManager.put("FileChooser.cancelButtonToolTipText", this.lang.nget("explorer.button.cancel.tip"));
      UIManager.put("FileChooser.updateButtonToolTipText", this.lang.nget("explorer.button.update.tip"));
      UIManager.put("FileChooser.helpButtonToolTipText", this.lang.nget("explorer.title.help.tip"));
      UIManager.put("FileChooser.directoryOpenButtonToolTipText", this.lang.nget("explorer.button.open-dir.tip"));
      UIManager.put("FileChooser.viewMenuLabelText", this.lang.nget("explorer.button.view"));
      UIManager.put("FileChooser.refreshActionLabelText", this.lang.nget("explorer.context.refresh"));
      UIManager.put("FileChooser.newFolderActionLabelText", this.lang.nget("explorer.context.newfolder"));
      UIManager.put("FileChooser.listViewActionLabelText", this.lang.nget("explorer.view.list"));
      UIManager.put("FileChooser.detailsViewActionLabelText", this.lang.nget("explorer.view.details"));
      UIManager.put("FileChooser.filesListAccessibleName", this.lang.nget("explorer.view.list.name"));
      UIManager.put("FileChooser.filesDetailsAccessibleName", this.lang.nget("explorer.view.details.name"));
      UIManager.put("FileChooser.renameErrorTitleText", this.lang.nget("explorer.error.rename.title"));
      UIManager.put("FileChooser.renameErrorText", this.lang.nget("explorer.error.rename") + "\n{0}");
      UIManager.put("FileChooser.renameErrorFileExistsText", this.lang.nget("explorer.error.rename-exists"));
      UIManager.put("FileChooser.readOnly", Boolean.FALSE);
      UIManager.put("TabbedPane.contentOpaque", false);
      UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
      UIManager.put("TabbedPane.tabInsets", new Insets(0, 8, 6, 8));
   }

   private void updateMaxPoint() {
      this.maxPoint.x = this.getWidth();
      this.maxPoint.y = this.getHeight();
   }

   public void setSize(int width, int height) {
      if (this.getWidth() != width || this.getHeight() != height) {
         if (this.getExtendedState() == 0) {
            boolean show = this.isVisible();
            if (show) {
               this.setVisible(false);
            }

            super.setSize(width, height);
            if (show) {
               this.setVisible(true);
               this.setLocationRelativeTo((Component)null);
            }

         }
      }
   }

   public void setSize(Dimension d) {
      this.setSize(d.width, d.height);
   }

   private static int getExtendedStateFor(int state) {
      switch(state) {
      case 0:
      case 2:
      case 4:
      case 6:
         return state;
      case 1:
      case 3:
      case 5:
      default:
         return -1;
      }
   }

   public static URL getRes(String uri) {
      return TLauncherFrame.class.getResource(uri);
   }

   private static void log(Object... o) {
      U.log("[Frame]", o);
   }
}
