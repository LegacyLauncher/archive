package com.turikhay.tlauncher;

import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.minecraft.profiles.ProfileLoader;
import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;
import com.turikhay.tlauncher.settings.ArgumentParser;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.Console;
import com.turikhay.tlauncher.ui.Localizable;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import com.turikhay.util.logger.MirroredLinkedStringStream;
import com.turikhay.util.logger.PrintLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;
import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionManager;

public class TLauncher {
   private static final double VERSION = 0.36D;
   private static final String SETTINGS = "tlauncher.ini";
   private static final String BRAND = "Original";
   private static final String[] DEFAULT_UPDATE_REPO = new String[]{"http://u.to/tlauncher-original/BlPcBA", "http://ru-minecraft.org/update/original.ini", "http://u.to/tlauncher-original-update/T4ASBQ", "http://5.9.120.11/update/original.ini", "http://u.to/tlauncher-original-update-mirror2/BIQSBQ", "http://dl.dropboxusercontent.com/u/6204017/update/original.ini"};
   private static final String[] REMOTE_REPO = new String[]{"http://s3.amazonaws.com/Minecraft.Download/"};
   private static final String[] EXTRA_REPO = new String[]{"http://5.9.120.11/update/versions/", "http://ru-minecraft.org/update/tlauncher/extra/", "http://dl.dropboxusercontent.com/u/6204017/update/versions/"};
   private static TLauncher instance;
   private static MirroredLinkedStringStream stream;
   private static PrintLogger print;
   private static Console console;
   private TLauncher.TLauncherState state;
   private Settings lang;
   private GlobalSettings settings;
   private Downloader downloader;
   private Updater updater;
   private TLauncherFrame frame;
   private TLauncherNoGraphics loader;
   private VersionManager vm;
   private ProfileLoader pl;
   private static final UUID clientToken = UUID.randomUUID();
   public final OptionSet args;
   public final String[] sargs;
   private MinecraftLauncher launcher;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState;

   public TLauncher(TLauncher.TLauncherState state, String[] sargs, OptionSet set) throws IOException {
      if (state == null) {
         throw new IllegalArgumentException("TLauncherState can't be NULL!");
      } else {
         U.log("TLauncher is loading in state", state);
         Time.start(this);
         instance = this;
         this.state = state;
         this.args = set;
         this.sargs = sargs;
         this.settings = GlobalSettings.createInstance(set);
         this.reloadLocale();
         console = new Console(this.settings, print, "TLauncher Dev Console", this.settings.getConsoleType() == GlobalSettings.ConsoleType.GLOBAL);
         Console.updateLocale();
         this.vm = new VersionManager();
         this.pl = new ProfileLoader();
         if (!this.settings.isStaticProfile()) {
            this.pl.createInto();
         } else {
            this.pl.createFrom();
         }

         this.init();
         U.log("Started! (" + Time.stop(this) + " ms.)");
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
         this.vm.addRefreshedListener(lf.buttons.addbuttons.refresh);
         this.updater.addListener(lf);
         this.updater.addListener(this.frame);
         this.updater.addListener(this.frame.getConnectionWarning());
         this.updater.addListener(lf.buttons.addbuttons.refresh);
         if (lf.autologin.isEnabled()) {
            this.vm.refreshVersions(true);
            lf.autologin.startLogin();
         } else {
            this.vm.asyncRefresh();
            this.updater.asyncFindUpdate();
         }
         break;
      case 2:
         this.downloader = new Downloader(1);
         this.loader = new TLauncherNoGraphics(this);
      }

      this.downloader.startLaunch();
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

   public static UUID getClientToken() {
      return clientToken;
   }

   public static Console getConsole() {
      return console;
   }

   public ProfileLoader getProfileLoader() {
      return this.pl;
   }

   public ProfileManager getCurrentProfileManager() {
      return this.pl.getSelected();
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
      this.launcher = new MinecraftLauncher(this, listener, forceupdate, true);
      this.launcher.start();
   }

   public boolean isLauncherWorking() {
      return this.launcher != null && this.launcher.isWorking();
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

   public static double getVersion() {
      return 0.36D;
   }

   public static String getBrand() {
      return "Original";
   }

   public static String[] getUpdateRepos() {
      return DEFAULT_UPDATE_REPO;
   }

   public static String getSettingsFile() {
      return "tlauncher.ini";
   }

   public static String[] getRemoteRepo() {
      return REMOTE_REPO;
   }

   public static String[] getExtraRepo() {
      return EXTRA_REPO;
   }

   public static void main(String[] args) {
      ExceptionHandler handler = ExceptionHandler.getInstance();
      Thread.setDefaultUncaughtExceptionHandler(handler);
      TLauncherFrame.initLookAndFeel();
      stream = new MirroredLinkedStringStream();
      stream.setMirror(System.out);
      print = new PrintLogger(stream);
      stream.setLogger(print);
      System.setOut(print);
      U.setPrefix("[TLauncher]");

      try {
         launch(args);
      } catch (Throwable var3) {
         Alert.showError(var3, true);
      }

   }

   private static void launch(String[] args) throws Exception {
      U.log("Hello!");
      U.log("---");
      U.log("Starting version", 0.36D);
      OptionSet set = ArgumentParser.parseArgs(args);
      if (set == null) {
         new TLauncher(TLauncher.TLauncherState.FULL, args, (OptionSet)null);
      } else {
         if (set.has("help")) {
            ArgumentParser.getParser().printHelpOn((OutputStream)System.out);
         }

         TLauncher.TLauncherState state = TLauncher.TLauncherState.FULL;
         if (set.has("nogui")) {
            state = TLauncher.TLauncherState.MINIMAL;
         }

         new TLauncher(state, args, set);
      }
   }

   public static TLauncher getInstance() {
      return instance;
   }

   public void newInstance() {
      Bootstrapper.main(this.sargs);
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
