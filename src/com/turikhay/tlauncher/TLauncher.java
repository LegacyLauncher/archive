package com.turikhay.tlauncher;

import com.google.gson.Gson;
import com.turikhay.tlauncher.configuration.ArgumentParser;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.managers.ComponentManager;
import com.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.managers.VersionManager;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.tlauncher.ui.listener.MinecraftUIListener;
import com.turikhay.tlauncher.ui.listener.UpdaterUIListener;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import com.turikhay.util.logger.MirroredLinkedStringStream;
import com.turikhay.util.logger.PrintLogger;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import joptsimple.OptionSet;
import net.minecraft.launcher.OperatingSystem;

public class TLauncher {
   private static final double VERSION = 0.811D;
   private static TLauncher instance;
   private static String[] sargs;
   private static File directory;
   private static PrintLogger print;
   private static Console console;
   private static Gson gson;
   private TLauncher.TLauncherState state;
   private LangConfiguration lang;
   private Configuration settings;
   private Downloader downloader;
   private Updater updater;
   private TLauncherFrame frame;
   private TLauncherLite loader;
   private ComponentManager manager;
   private VersionManager versionManager;
   private ProfileManager profileManager;
   private final OptionSet args;
   private MinecraftLauncher launcher;
   private UpdaterUIListener updaterListener;
   private MinecraftUIListener minecraftListener;
   private boolean ready;
   private static final String SETTINGS = "tlauncher.cfg";
   private static final String BRAND = "Original";
   private static final String FOLDER = "minecraft";
   private static final String[] DEFAULT_UPDATE_REPO = new String[]{"http://u.to/tlauncher-original-update-mirror-3/D9wMBg", "http://s1.mmods.ru/launcher/original.ini", "http://u.to/tlauncher-original/BlPcBA", "http://ru-minecraft.org/update/original.ini", "http://u.to/tlauncher-original-update/T4ASBQ", "http://5.9.120.11/update/original.ini", "http://u.to/tlauncher-original-update-mirror2/BIQSBQ", "http://dl.dropboxusercontent.com/u/6204017/update/original.ini"};
   private static final String[] OFFICIAL_REPO = new String[]{"http://s3.amazonaws.com/Minecraft.Download/"};
   private static final String[] EXTRA_REPO = new String[]{"http://5.9.120.11/update/versions/", "http://s1.mmods.ru/launcher/", "http://dl.dropboxusercontent.com/u/6204017/update/versions/"};
   private static final String[] FORGE_REPO = new String[0];
   private static final String[] LIBRARY_REPO = new String[]{"https://libraries.minecraft.net/"};
   private static final String[] ASSETS_REPO = new String[]{"http://resources.download.minecraft.net/"};
   private static final String[] SERVER_LIST = new String[0];
   private static final String[] MOD_LIST = new String[0];
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState;

   private TLauncher(TLauncher.TLauncherState state, OptionSet set) throws Exception {
      if (state == null) {
         throw new IllegalArgumentException("TLauncherState can't be NULL!");
      } else {
         U.log("TLauncher is loading in state", state);
         Time.start(this);
         instance = this;
         this.state = state;
         this.args = set;
         gson = new Gson();
         this.settings = Configuration.createConfiguration(set);
         this.reloadLocale();
         console = new Console(this.settings, print, "TLauncher Dev Console", this.settings.getConsoleType() == Configuration.ConsoleType.GLOBAL);
         if (state.equals(TLauncher.TLauncherState.MINIMAL)) {
            console.setCloseAction(Console.CloseAction.KILL);
         }

         Console.updateLocale();
         this.manager = new ComponentManager(this);
         this.versionManager = (VersionManager)this.manager.loadComponent(VersionManager.class);
         this.profileManager = (ProfileManager)this.manager.loadComponent(ProfileManager.class);
         this.manager.loadComponent(ComponentManagerListenerHelper.class);
         this.init();
         U.log("Started! (" + Time.stop(this) + " ms.)");
         FileUtil.deleteFile(MinecraftUtil.getSystemRelatedFile("tlauncher.ini"));
         this.ready = true;
      }
   }

   private void init() {
      this.downloader = new Downloader(this);
      this.minecraftListener = new MinecraftUIListener(this);
      switch($SWITCH_TABLE$com$turikhay$tlauncher$TLauncher$TLauncherState()[this.state.ordinal()]) {
      case 1:
         this.updaterListener = new UpdaterUIListener(this);
         this.updater = new Updater(this);
         this.updater.addListener(this.updaterListener);
         this.frame = new TLauncherFrame(this);
         LoginForm lf = this.frame.mp.defaultScene.loginForm;
         if (lf.autologin.isEnabled()) {
            this.versionManager.startRefresh(true);
            lf.autologin.setActive(true);
         } else {
            this.versionManager.asyncRefresh();
            this.updater.asyncFindUpdate();
         }

         this.profileManager.refresh();
         break;
      case 2:
         this.loader = new TLauncherLite(this);
      }

   }

   public Downloader getDownloader() {
      return this.downloader;
   }

   public LangConfiguration getLang() {
      return this.lang;
   }

   public Configuration getSettings() {
      return this.settings;
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

   public TLauncherLite getLoader() {
      return this.loader;
   }

   public static Console getConsole() {
      return console;
   }

   public static Gson getGson() {
      return gson;
   }

   public ComponentManager getManager() {
      return this.manager;
   }

   public VersionManager getVersionManager() {
      return this.versionManager;
   }

   public ProfileManager getProfileManager() {
      return this.profileManager;
   }

   public MinecraftLauncher getLauncher() {
      return this.launcher;
   }

   public UpdaterUIListener getUpdaterListener() {
      return this.updaterListener;
   }

   public MinecraftUIListener getMinecraftListener() {
      return this.minecraftListener;
   }

   public boolean isReady() {
      return this.ready;
   }

   public void reloadLocale() throws IOException {
      Locale locale = this.settings.getLocale();
      U.log("Selected locale: " + locale);
      if (this.lang == null) {
         this.lang = new LangConfiguration(this.settings.getLocales(), locale);
      } else {
         this.lang.setSelected(locale);
      }

      Localizable.setLang(this.lang);
      Alert.prepareLocal();
   }

   public void launch(MinecraftListener listener, boolean forceupdate) {
      this.launcher = new MinecraftLauncher(this, forceupdate);
      this.launcher.addListener(this.minecraftListener);
      this.launcher.addListener(listener);
      this.launcher.addListener(this.frame.mp.getProgress());
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
      if (this.frame != null) {
         this.frame.setVisible(false);
      }

   }

   public void show() {
      U.log("Here I am!");
      if (this.frame != null) {
         this.frame.setVisible(true);
      }

   }

   public static void main(String[] args) {
      ExceptionHandler handler = ExceptionHandler.getInstance();
      Thread.setDefaultUncaughtExceptionHandler(handler);
      U.setPrefix("[TLauncher]");
      MirroredLinkedStringStream stream = new MirroredLinkedStringStream();
      stream.setMirror(System.out);
      print = new PrintLogger(stream);
      stream.setLogger(print);
      System.setOut(print);
      TLauncherFrame.initLookAndFeel();

      try {
         launch(args);
      } catch (Throwable var4) {
         U.log("Error launching TLauncher:");
         var4.printStackTrace(print);
         Alert.showError(var4, true);
      }

   }

   private static void launch(String[] args) throws Exception {
      U.log("Hello!");
      U.log("Starting TLauncher", "Original", 0.811D);
      U.log("Machine info:", OperatingSystem.getCurrentInfo());
      U.log("---");
      sargs = args;
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

   public static String[] getArgs() {
      if (sargs == null) {
         sargs = new String[0];
      }

      return sargs;
   }

   public static File getDirectory() {
      if (directory == null) {
         directory = new File(".");
      }

      return directory;
   }

   public static TLauncher getInstance() {
      return instance;
   }

   public void newInstance() {
      Bootstrapper.main(sargs);
   }

   public static double getVersion() {
      return 0.811D;
   }

   public static String getBrand() {
      return "Original";
   }

   public static String getFolder() {
      return "minecraft";
   }

   public static String[] getUpdateRepos() {
      return DEFAULT_UPDATE_REPO;
   }

   public static String getSettingsFile() {
      return "tlauncher.cfg";
   }

   public static String[] getOfficialRepo() {
      return OFFICIAL_REPO;
   }

   public static String[] getExtraRepo() {
      return EXTRA_REPO;
   }

   public static String[] getForgeRepo() {
      return FORGE_REPO;
   }

   public static String[] getLibraryRepo() {
      return LIBRARY_REPO;
   }

   public static String[] getAssetsRepo() {
      return ASSETS_REPO;
   }

   public static String[] getServerList() {
      return SERVER_LIST;
   }

   public static String[] getModList() {
      return MOD_LIST;
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
