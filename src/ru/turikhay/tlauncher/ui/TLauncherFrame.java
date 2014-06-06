package ru.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.UIManager;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class TLauncherFrame extends JFrame {
   private static final long serialVersionUID = 5077131443679431434L;
   public static final int[] maxSize = new int[]{1920, 1080};
   public static final float fontSize = 12.0F;
   private final TLauncherFrame instance = this;
   private final TLauncher tlauncher;
   private final Configuration settings;
   private final LangConfiguration lang;
   private final int[] windowSize;
   public final MainPane mp;

   public TLauncherFrame(TLauncher t) {
      this.tlauncher = t;
      this.settings = t.getSettings();
      this.lang = t.getLang();
      this.windowSize = this.settings.getWindowSize();
      SwingUtil.initFontSize(12);
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
      this.addComponentListener(new ComponentListener() {
         public void componentResized(ComponentEvent e) {
            TLauncherFrame.this.mp.onResize();
         }

         public void componentShown(ComponentEvent e) {
            TLauncherFrame.this.instance.validate();
            TLauncherFrame.this.instance.repaint();
            TLauncherFrame.this.instance.toFront();
            TLauncherFrame.this.mp.background.startBackground();
         }

         public void componentMoved(ComponentEvent e) {
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
      log("Preparing main pane...");
      this.mp = new MainPane(this);
      this.add(this.mp);
      log("Packing main frame...");
      this.pack();
      log("Resizing main pane...");
      this.mp.onResize();
      this.mp.background.startBackground();
      int windowState = getExtendedStateFor(this.settings.getInteger("gui.window"));
      if (windowState != -1) {
         this.setExtendedState(windowState);
      }

      this.setVisible(true);
      if (this.settings.isFirstRun()) {
         Alert.showLocAsyncWarning("firstrun");
      }

   }

   public TLauncher getLauncher() {
      return this.tlauncher;
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
      String brand = TLauncher.getBrand() + " " + TLauncher.getVersion();
      this.setTitle("TLauncher " + brand + " " + copyright);
   }

   private void setWindowSize() {
      int width = this.windowSize[0] > maxSize[0] ? maxSize[0] : this.windowSize[0];
      int height = this.windowSize[1] > maxSize[1] ? maxSize[1] : this.windowSize[1];
      Dimension size = new Dimension(width, height);
      this.setPreferredSize(size);
      this.setMinimumSize(size);
      this.setLocationRelativeTo((Component)null);
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
