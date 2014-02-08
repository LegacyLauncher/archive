package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import com.turikhay.util.U;
import java.awt.Component;
import java.awt.Container;
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
   private static final List favicons = new ArrayList();
   private final TLauncherFrame instance = this;
   public final TLauncher tlauncher;
   public final Configuration settings;
   public final LangConfiguration lang;
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
            TLauncherFrame.this.mp.showBackground();
         }

         public void componentMoved(ComponentEvent e) {
         }

         public void componentHidden(ComponentEvent e) {
            TLauncherFrame.this.mp.hideBackground();
         }
      });
      log("Preparing main pane...");
      this.mp = new MainPane(this);
      this.add(this.mp);
      log("Packing main frame...");
      this.pack();
      log("Resizing main pane...");
      this.mp.onResize();
      this.mp.showBackground();
      this.setVisible(true);
      if (this.settings.isFirstRun()) {
         Alert.showAsyncWarning("firstrun");
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
      updateContainer(this, true);
   }

   private void setWindowSize() {
      Dimension sizes = new Dimension(this.windowSize[0], this.windowSize[1]);
      this.setPreferredSize(sizes);
      this.setMinimumSize(sizes);
      this.setLocationRelativeTo((Component)null);
   }

   private void setWindowTitle() {
      String translator = this.lang.nget("translator");
      String copyright = "(by turikhay" + (translator != null ? ", translated by " + translator : "") + ")";
      String brand = TLauncher.getBrand() + " " + TLauncher.getVersion();
      this.setTitle("TLauncher " + brand + " " + copyright);
   }

   private void setUILocale() {
      UIManager.put("OptionPane.yesButtonText", this.lang.nget("ui.yes"));
      UIManager.put("OptionPane.noButtonText", this.lang.nget("ui.no"));
      UIManager.put("OptionPane.cancelButtonText", this.lang.nget("ui.cancel"));
   }

   public static void initFontSize() {
      try {
         UIDefaults defaults = UIManager.getDefaults();
         Enumeration e = defaults.keys();

         while(e.hasMoreElements()) {
            Object key = e.nextElement();
            Object value = defaults.get(key);
            if (value instanceof Font) {
               Font font = (Font)value;
               int size = font.getSize();
               if (size < 12) {
                  size = 12;
               } else if (size > 14) {
                  size = 14;
               }

               if (value instanceof FontUIResource) {
                  defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), size));
               } else {
                  defaults.put(key, new Font(font.getName(), font.getStyle(), size));
               }
            }
         }
      } catch (Exception var6) {
         log("Cannot change font sizes!", var6);
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

   public static void updateContainer(Container container, boolean deep) {
      Component[] var5;
      int var4 = (var5 = container.getComponents()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Component c = var5[var3];
         if (c instanceof LocalizableComponent) {
            ((LocalizableComponent)c).updateLocale();
         }

         if (c instanceof Container && deep) {
            updateContainer((Container)c, true);
         }
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
