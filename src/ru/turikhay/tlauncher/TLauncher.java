package ru.turikhay.tlauncher;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.configuration.ArgumentParser;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.handlers.SimpleHostnameVerifier;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.ui.listener.MinecraftUIListener;
import ru.turikhay.tlauncher.ui.listener.RequiredUpdateListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.stream.MirroredLinkedStringStream;
import ru.turikhay.util.stream.PrintLogger;

public class TLauncher {
   private static final double VERSION = 1.39D;
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
   private TLauncherLite lite;
   private ComponentManager manager;
   private VersionManager versionManager;
   private ProfileManager profileManager;
   private final OptionSet args;
   private MinecraftLauncher launcher;
   private RequiredUpdateListener updateListener;
   private MinecraftUIListener minecraftListener;
   private boolean ready;
   private static final String SETTINGS = "tlauncher/legacy.properties";
   private static final String BRAND = "Legacy";
   private static final String FOLDER = "minecraft";
   private static final String DEVELOPER = "turikhay";
   private static final String[] DEFAULT_UPDATE_REPO = new String[]{"http://u.to/tlauncher-legacy-update-mirror-3/vFqOCA", "http://s1.mmods.ru/launcher/legacy.ini", "http://u.to/tlauncher-original/BlPcBA", "http://ru-minecraft.org/update/original.ini", "http://u.to/tlauncher-original-update/T4ASBQ", "http://5.9.120.11/update/original.ini", "http://u.to/tlauncher-original-update-mirror2/BIQSBQ", "http://dl.dropboxusercontent.com/u/6204017/update/original.ini"};
   private static final String[] OFFICIAL_REPO = new String[]{"http://s3.amazonaws.com/Minecraft.Download/"};
   private static final String[] EXTRA_REPO = new String[]{"http://s1.mmods.ru/launcher/", "http://5.9.120.11/update/versions/"};
   private static final String[] LIBRARY_REPO = new String[]{"https://libraries.minecraft.net/", "http://5.9.120.11/update/versions/libraries/", "http://s1.mmods.ru/launcher/libraries/"};
   private static final String[] ASSETS_REPO = new String[]{"http://resources.download.minecraft.net/"};
   private static final String[] SERVER_LIST = new String[0];
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$TLauncher$TLauncherState;

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
         File oldConfig = MinecraftUtil.getSystemRelatedFile("tlauncher.cfg");
         File newConfig = MinecraftUtil.getSystemRelatedDirectory("tlauncher/legacy.properties");
         if (!oldConfig.isFile()) {
            oldConfig = MinecraftUtil.getSystemRelatedFile(".tlauncher/tlauncher.properties");
         }

         if (oldConfig.isFile() && !newConfig.isFile()) {
            boolean copied = true;

            try {
               FileUtil.createFile(newConfig);
               FileUtil.copyFile(oldConfig, newConfig, true);
            } catch (IOException var7) {
               U.log("Cannot copy old configuration to the new place", oldConfig, newConfig, var7);
               copied = false;
            }

            if (copied) {
               U.log("Old configuration successfully moved to the new place:", newConfig);
               FileUtil.deleteFile(oldConfig);
            }
         }

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
         this.ready = true;
      }
   }

   private void init() {
      this.downloader = new Downloader(this);
      this.minecraftListener = new MinecraftUIListener(this);
      switch($SWITCH_TABLE$ru$turikhay$tlauncher$TLauncher$TLauncherState()[this.state.ordinal()]) {
      case 1:
         this.updater = new Updater(this);
         this.updateListener = new RequiredUpdateListener(this.updater);
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
         this.lite = new TLauncherLite(this);
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
      return this.lite;
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

   public MinecraftUIListener getMinecraftListener() {
      return this.minecraftListener;
   }

   public RequiredUpdateListener getUpdateListener() {
      return this.updateListener;
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
      U.log("I'm hiding!");
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
      Thread.currentThread().setUncaughtExceptionHandler(handler);
      HttpsURLConnection.setDefaultHostnameVerifier(SimpleHostnameVerifier.getInstance());
      U.setPrefix("[TLauncher]");
      MirroredLinkedStringStream stream = new MirroredLinkedStringStream();
      stream.setMirror(System.out);
      print = new PrintLogger(stream);
      stream.setLogger(print);
      System.setOut(print);
      SwingUtil.initLookAndFeel();

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
      U.log("Starting TLauncher", "Legacy", 1.39D, "by", "turikhay");
      U.log("Have question? Find my e-mail in lang files.");
      U.log("Machine info:", OS.getSummary());
      U.log("Startup time:", Calendar.getInstance().getTime());
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
      return 1.39D;
   }

   public static String getBrand() {
      return "Legacy";
   }

   public static String getDeveloper() {
      return "turikhay";
   }

   public static String getFolder() {
      return "minecraft";
   }

   public static String[] getUpdateRepos() {
      return DEFAULT_UPDATE_REPO;
   }

   public static String getSettingsFile() {
      return "tlauncher/legacy.properties";
   }

   public static String[] getOfficialRepo() {
      return OFFICIAL_REPO;
   }

   public static String[] getExtraRepo() {
      return EXTRA_REPO;
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

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$TLauncher$TLauncherState() {
      int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$TLauncher$TLauncherState;
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

         $SWITCH_TABLE$ru$turikhay$tlauncher$TLauncher$TLauncherState = var0;
         return var0;
      }
   }

   public static enum TLauncherState {
      FULL,
      MINIMAL;
   }
}
