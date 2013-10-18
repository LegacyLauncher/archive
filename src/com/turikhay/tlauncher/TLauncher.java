package com.turikhay.tlauncher;

import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.ArgumentParser;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.Localizable;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.util.U;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import joptsimple.OptionSet;
import net.minecraft.launcher_.updater.VersionManager;

public class TLauncher {
   public static final double VERSION = 0.178D;
   private static TLauncher instance;
   private TLauncher.TLauncherState state;
   private Settings lang;
   private GlobalSettings settings;
   private Downloader downloader;
   private Updater updater;
   private TLauncherFrame frame;
   private TLauncherNoGraphics loader;
   private VersionManager vm;
   private OptionSet args;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState;

   public TLauncher(TLauncher.TLauncherState state, OptionSet set) throws IOException {
      Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
      if (state == null) {
         throw new IllegalArgumentException("TLauncherState can't be NULL!");
      } else {
         U.log("State:", state);
         instance = this;
         this.state = state;
         this.args = set;
         long start = System.currentTimeMillis();
         this.settings = GlobalSettings.createInstance(set);
         this.reloadLocale();
         this.vm = new VersionManager();
         this.init();
         long end = System.currentTimeMillis();
         long diff = end - start;
         U.log("Started! (" + diff + " ms.)");
      }
   }

   private void init() {
      switch($SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState()[this.state.ordinal()]) {
      case 1:
         this.downloader = new Downloader(10);
         this.updater = new Updater(this);
         this.frame = new TLauncherFrame(this);
         LoginForm lf = this.frame.getLoginForm();
         this.vm.addRefreshedListener(lf.versionchoice);
         this.updater.addListener(this.frame);
         if (lf.autologin.isEnabled()) {
            this.vm.refreshVersions(true);
            lf.autologin.startLogin();
         } else {
            this.vm.asyncRefresh();
            this.vm.asyncRefreshResources();
            this.updater.asyncFindUpdate();
         }
         break;
      case 2:
         this.downloader = new Downloader(1);
         this.loader = new TLauncherNoGraphics(this);
      }

      this.downloader.launch();
   }

   public Downloader getDownloader() {
      return this.downloader;
   }

   public Settings getLang() {
      return this.lang;
   }

   public GlobalSettings getSettings() {
      return this.settings;
   }

   public VersionManager getVersionManager() {
      return this.vm;
   }

   public Updater getUpdater() {
      return this.updater;
   }

   public OptionSet getArguments() {
      return this.args;
   }

   public TLauncherFrame getFrame() {
      return this.frame;
   }

   public TLauncherNoGraphics getLoader() {
      return this.loader;
   }

   public void reloadLocale() throws IOException {
      Locale locale = this.settings.getLocale();
      U.log("Selected locale: " + locale);
      URL url = TLauncher.class.getResource("/lang/" + locale + ".ini");
      if (this.lang == null) {
         this.lang = new Settings(url);
      } else {
         this.lang.reload(url);
      }

      Alert.prepareLocal();
      Localizable.setLang(this.lang);
   }

   public void launch(MinecraftLauncherListener listener, boolean forceupdate) {
      MinecraftLauncher launcher = new MinecraftLauncher(this, listener, forceupdate, true);
      launcher.start();
   }

   public static void kill() {
      U.log("Good bye!");
      System.exit(0);
   }

   public void hide() {
      U.log("Hiding...");
      this.frame.setVisible(false);
   }

   public void show() {
      U.log("Here I am!");
      this.frame.setVisible(true);
   }

   public static void main(String[] args) {
      ExceptionHandler handler = ExceptionHandler.getInstance();
      Thread.setDefaultUncaughtExceptionHandler(handler);

      try {
         launch(args);
      } catch (Throwable var3) {
         var3.printStackTrace();
         Alert.showError(var3, true);
      }

   }

   private static void launch(String[] args) throws Exception {
      U.log("Hello!");
      U.log("TLauncher mustn't be used for profit, guys. I hope you understand.");
      U.log("---");
      U.log("Starting version 0.178...");
      OptionSet set = ArgumentParser.parseArgs(args);
      if (set == null) {
         new TLauncher(TLauncher.TLauncherState.FULL, (OptionSet)null);
      } else {
         if (set.has("help")) {
            ArgumentParser.getParser().printHelpOn((OutputStream)System.out);
         }

         TLauncher.TLauncherState state = TLauncher.TLauncherState.FULL;
         if (set.has("nogui")) {
            state = TLauncher.TLauncherState.MINIMAL;
         }

         new TLauncher(state, set);
      }
   }

   public static TLauncher getInstance() {
      return instance;
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[TLauncher.TLauncherState.values().length];

         try {
            var0[TLauncher.TLauncherState.FULL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[TLauncher.TLauncherState.MINIMAL.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState = var0;
         return var0;
      }
   }

   public static enum TLauncherState {
      FULL,
      MINIMAL;
   }
}
