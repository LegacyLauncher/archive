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
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.UIManager;
import ru.turikhay.tlauncher.Bootstrapper;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
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
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.async.ExtendedThread;

public class TLauncherFrame extends JFrame {
   public static final Dimension minSize = new Dimension(530, 550);
   public static final Dimension maxSize = new Dimension(1920, 1080);
   public static final float fontSize;
   private final TLauncherFrame instance = this;
   private final TLauncher tlauncher;
   private final Configuration settings;
   private final LangConfiguration lang;
   private final int[] windowSize;
   private final Point maxPoint;
   public final MainPane mp;
   private String brand;
   private SimpleConfiguration uiConfig;

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
      this.setupUI();
      this.updateUILocale();
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
      if (TLauncher.isBeta()) {
         new TLauncherFrame.TitleUpdaterThread();
      } else {
         this.setWindowTitle();
      }

      this.setVisible(true);
      int windowState = getExtendedStateFor(this.settings.getInteger("gui.window"));
      if (windowState == 0) {
         this.setLocationRelativeTo((Component)null);
      } else {
         this.setExtendedState(windowState);
      }

      if (this.settings.isFirstRun()) {
         AsyncThread.execute(new Runnable() {
            public void run() {
               Alert.showLocWarning("firstrun");
            }
         });
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
      this.updateUILocale();
      Localizable.updateContainer(this);
   }

   public void updateTitle() {
      StringBuilder brandBuilder = (new StringBuilder()).append(TLauncher.getBrand()).append(' ').append(TLauncher.getVersion());
      if (TLauncher.getDebug()) {
         brandBuilder.append(" [DEBUG]");
      }

      if (TLauncher.isBeta()) {
         brandBuilder.append(" [BETA]");
      }

      this.brand = brandBuilder.toString();
   }

   public void setWindowTitle() {
      this.updateTitle();
      String title;
      if (TLauncher.isBeta()) {
         title = String.format("TLauncher %s [%s]", this.brand, U.memoryStatus());
      } else {
         title = String.format("TLauncher %s", this.brand);
      }

      this.setTitle(title);
   }

   private void setWindowSize() {
      int width = this.windowSize[0] > maxSize.width ? maxSize.width : this.windowSize[0];
      int height = this.windowSize[1] > maxSize.height ? maxSize.height : this.windowSize[1];
      Dimension curSize = new Dimension(width, height);
      this.setMinimumSize(minSize);
      this.setPreferredSize(curSize);
   }

   private void setupUI() {
      UIManager.put("FileChooser.newFolderErrorSeparator", ": ");
      UIManager.put("FileChooser.readOnly", Boolean.FALSE);
      UIManager.put("TabbedPane.contentOpaque", false);
      UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
      UIManager.put("TabbedPane.tabInsets", new Insets(0, 8, 6, 8));
   }

   private void updateUILocale() {
      if (this.uiConfig == null) {
         try {
            this.uiConfig = new SimpleConfiguration(this.getClass().getResource("/lang/_ui"));
         } catch (Exception var4) {
            return;
         }
      }

      Iterator var2 = this.uiConfig.getKeys().iterator();

      while(var2.hasNext()) {
         String key = (String)var2.next();
         String value = this.uiConfig.get(key);
         if (value != null) {
            UIManager.put(key, this.lang.get(value));
         }
      }

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

   private class TitleUpdaterThread extends ExtendedThread {
      TitleUpdaterThread() {
         super("TitleUpdater");
         TLauncherFrame.this.updateTitle();
         this.start();
      }

      public void run() {
         while(TLauncherFrame.this.isDisplayable()) {
            U.sleepFor(100L);
            TLauncherFrame.this.setWindowTitle();
         }

         TLauncherFrame.log("Title updater is shut down.");
         this.interrupt();
      }
   }
}
