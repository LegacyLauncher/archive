package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import com.turikhay.util.U;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class TLauncherFrame extends JFrame {
   private static final long serialVersionUID = 5077131443679431434L;
   public static final int[] maxSize = new int[]{1920, 1080};
   private static final List favicons = new ArrayList();
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
      initLookAndFeel();
      initFontSize();
      this.setUILocale();
      this.setWindowSize();
      this.setWindowTitle();
      this.setIconImages(getFavicons());
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
      log("Preparing main pane...");
      this.mp = new MainPane(this);
      this.add(this.mp);
      log("Packing main frame...");
      this.pack();
      log("Resizing main pane...");
      this.mp.onResize();
      this.mp.background.startBackground();
      this.setVisible(true);
      if (this.settings.isFirstRun()) {
         Alert.showLocAsyncWarning("firstrun");
      }

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
      String copyright = "(by turikhay" + (translator != null ? ", translated by " + translator : "") + ")";
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
      UIManager.put("FileChooser.readOnly", Boolean.TRUE);
   }

   private static void initFontSize() {
      try {
         UIDefaults defaults = UIManager.getDefaults();
         int minSize = 12;
         int maxSize = 14;
         Enumeration e = defaults.keys();

         while(e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font) {
               Font font = (Font)value;
               int size = font.getSize();
               if (size < minSize) {
                  size = minSize;
               } else if (size > maxSize) {
                  size = maxSize;
               }

               if (value instanceof FontUIResource) {
                  defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), size));
               } else {
                  defaults.put(key, new Font(font.getName(), font.getStyle(), size));
               }
            }
         }
      } catch (Exception var8) {
         log("Cannot change font sizes!", var8);
      }

   }

   public static void initLookAndFeel() {
      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception var1) {
         log("Can't set system look and feel.");
         var1.printStackTrace();
      }

   }

   public static List getFavicons() {
      if (!favicons.isEmpty()) {
         return Collections.unmodifiableList(favicons);
      } else {
         int[] sizes = new int[]{256, 128, 96, 64, 48, 32, 24, 16};
         String loaded = "";
         int[] var5 = sizes;
         int var4 = sizes.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            int i = var5[var3];
            Image image = ImageCache.getImage("fav" + i + ".png", false);
            if (image != null) {
               loaded = loaded + ", " + i + "px";
               favicons.add(image);
            }
         }

         if (loaded.isEmpty()) {
            log("No favicon is loaded.");
         } else {
            log("Favicons loaded:", loaded.substring(2));
         }

         return favicons;
      }
   }

   public static URL getRes(String uri) {
      return TLauncherFrame.class.getResource(uri);
   }

   private static void log(Object... o) {
      U.log("[Frame]", o);
   }
}
