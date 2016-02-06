package ru.turikhay.tlauncher;

import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.configuration.ArgumentParser;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.configuration.Static;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import ru.turikhay.tlauncher.managers.ElyManager;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.ui.listener.MinecraftUIListener;
import ru.turikhay.tlauncher.ui.listener.RequiredUpdateListener;
import ru.turikhay.tlauncher.ui.listener.VersionManagerUIListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.RunnableThread;
import ru.turikhay.util.stream.MirroredLinkedOutputStringStream;
import ru.turikhay.util.stream.PrintLogger;

public class TLauncher {
   private static TLauncher instance;
   private static String[] sargs;
   private static File directory;
   private static PrintLogger print;
   private static Console console;
   private static Gson gson;
   private LangConfiguration lang;
   private Configuration settings;
   private Downloader downloader;
   private Updater updater;
   private TLauncherFrame frame;
   private ComponentManager manager;
   private VersionManager versionManager;
   private ProfileManager profileManager;
   private ElyManager elyManager;
   private final OptionSet args;
   private MinecraftLauncher launcher;
   private RequiredUpdateListener updateListener;
   private MinecraftUIListener minecraftListener;
   private VersionManagerUIListener vmListener;
   private boolean ready;
   private static boolean useSystemLookAndFeel = true;

   private TLauncher(OptionSet set) throws Exception {
      Time.start(this);
      instance = this;
      this.args = set;
      gson = new Gson();
      U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_CONFIGURATION);
      this.settings = Configuration.createConfiguration(set);
      useSystemLookAndFeel &= this.settings.getBoolean("gui.systemlookandfeel");
      TLauncherFrame.setFontSize(this.settings.getFloat("gui.font"));
      this.reloadLocale();
      if (useSystemLookAndFeel) {
         U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_LOOKANDFEEL);
         useSystemLookAndFeel = SwingUtil.initLookAndFeel();
      }

      this.settings.set("gui.systemlookandfeel", useSystemLookAndFeel, false);
      U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_CONSOLE);
      console = new Console(this.settings, print, "Logger", this.settings.getConsoleType() == Configuration.ConsoleType.GLOBAL);
      console.setCloseAction(Console.CloseAction.KILL);
      Console.updateLocale();
      U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_MANAGERS);
      this.manager = new ComponentManager(this);
      this.settings.set("minecraft.gamedir", MinecraftUtil.getWorkingDirectory(), false);
      this.elyManager = (ElyManager)this.manager.loadComponent(ElyManager.class);
      this.versionManager = (VersionManager)this.manager.loadComponent(VersionManager.class);
      this.profileManager = (ProfileManager)this.manager.loadComponent(ProfileManager.class);
      this.manager.loadComponent(ComponentManagerListenerHelper.class);
      this.init();
      this.ready = true;
      U.log("Started! (" + Time.stop(this) + " ms.)");
      U.setLoadingStep(Bootstrapper.LoadingStep.SUCCESS);
   }

   private void init() {
      this.downloader = new Downloader(this);
      this.minecraftListener = new MinecraftUIListener(this);
      this.vmListener = new VersionManagerUIListener(this);
      this.updater = new Updater();
      this.updateListener = new RequiredUpdateListener(this.updater);
      U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_WINDOW);
      this.frame = new TLauncherFrame(this);
      LoginForm lf = this.frame.mp.defaultScene.loginForm;
      U.setLoadingStep(Bootstrapper.LoadingStep.REFRESHING_INFO);
      if (lf.autologin.isEnabled()) {
         this.versionManager.startRefresh(true);
         lf.autologin.setActive(true);
      } else {
         this.versionManager.asyncRefresh();
         this.updater.asyncFindUpdate();
      }

      this.profileManager.refresh();
      (new RunnableThread("UpdaterWatchdog", new Runnable() {
         public void run() {
            while(true) {
               U.sleepFor(1800000L);
               TLauncher.this.updater.asyncFindUpdate();
            }
         }
      })).start();
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

   public TLauncherFrame getFrame() {
      return this.frame;
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

   public ElyManager getElyManager() {
      return this.elyManager;
   }

   public MinecraftLauncher getLauncher() {
      return this.launcher;
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
      if (console != null) {
         console.setName(this.lang.get("console"));
      }

   }

   public void launch(MinecraftListener listener, ServerList.Server server, boolean forceupdate) {
      this.launcher = new MinecraftLauncher(this, forceupdate);
      this.launcher.addListener(this.minecraftListener);
      this.launcher.addListener(listener);
      this.launcher.addListener(this.frame.mp.getProgress());
      this.launcher.setServer(server);
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
      if (this.frame != null) {
         boolean doAgain = true;

         while(doAgain) {
            try {
               this.frame.setVisible(false);
               doAgain = false;
            } catch (Exception var3) {
            }
         }
      }

      U.log("I'm hiding!");
   }

   public void show() {
      if (this.frame != null) {
         boolean doAgain = true;

         while(doAgain) {
            try {
               this.frame.setVisible(true);
               doAgain = false;
            } catch (Exception var3) {
            }
         }
      }

      U.log("Here I am!");
   }

   public static void main(String[] args) {
      Bootstrapper.checkRunningPath();
      ExceptionHandler handler = ExceptionHandler.getInstance();
      Thread.setDefaultUncaughtExceptionHandler(handler);
      Thread.currentThread().setUncaughtExceptionHandler(handler);
      U.setPrefix(">>");
      MirroredLinkedOutputStringStream stream = new MirroredLinkedOutputStringStream() {
         public void flush() {
            if (TLauncher.console == null) {
               try {
                  this.getMirror().flush();
               } catch (IOException var2) {
               }
            } else {
               super.flush();
            }

         }
      };
      stream.setMirror(System.out);
      print = new PrintLogger(stream);
      stream.setLogger(print);
      System.setOut(print);
      U.setLoadingStep(Bootstrapper.LoadingStep.INITALIZING);

      try {
         launch(args);
      } catch (Throwable var11) {
         Throwable e = var11;
         U.log("Error launching TLauncher:");
         var11.printStackTrace(print);
         StackTraceElement[] var7;
         int var6 = (var7 = var11.getStackTrace()).length;

         for(int var5 = 0; var5 < var6; ++var5) {
            StackTraceElement stE = var7[var5];
            if (stE.toString().toLowerCase().contains("lookandfeel")) {
               U.log("Found problem with L&F at:", stE);
               if (useSystemLookAndFeel) {
                  U.log("System L&F was used. Trying to reinit without it.");
                  SwingUtil.resetLookAndFeel();
                  useSystemLookAndFeel = false;

                  try {
                     launch(args);
                     e = null;
                  } catch (Throwable var10) {
                     e = var10;
                  }

                  if (e == null) {
                     return;
                  }
               } else {
                  U.log("Default L&F was used. Nothing to do with it.");
               }
               break;
            }
         }

         Alert.showError(e, true);
      }

   }

   private static void launch(String[] args) throws Exception {
      U.log("Starting TLauncher", getVersion(), getBrand());
      if (isBeta() || getDebug()) {
         U.log("Beta:", isBeta(), ", debug:", getDebug());
      }

      U.log("Machine info:", OS.getSummary());
      U.log("Startup time:", DateFormat.getDateTimeInstance(3, 1).format(new Date()));
      U.log("---");
      sargs = args;
      new TLauncher(ArgumentParser.parseArgs(args, print));
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

   public static double getVersion() {
      return 1.7D;
   }

   public static boolean isBeta() {
      return true;
   }

   public static boolean getDebug() {
      return false;
   }

   public static String getBrand() {
      return Static.getBrand();
   }

   public static String getDeveloper() {
      return "turikhay";
   }

   public static String getFolder() {
      return Static.getFolder();
   }

   public static String[] getUpdateRepos() {
      return Static.getUpdateRepo();
   }

   public static String getSettingsFile() {
      return Static.getSettings();
   }

   public static String[] getOfficialRepo() {
      return Static.getOfficialRepo();
   }

   public static String[] getExtraRepo() {
      return Static.getExtraRepo();
   }

   public static String[] getLibraryRepo() {
      return Static.getLibraryRepo();
   }

   public static String[] getAssetsRepo() {
      return Static.getAssetsRepo();
   }

   public static String[] getServerList() {
      return Static.getServerList();
   }

   static {
      try {
         SSLContext context = SSLContext.getInstance("SSL");
         context.init((KeyManager[])null, new X509TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
               return new X509Certificate[0];
            }
         }}, (SecureRandom)null);
         HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
      } catch (Throwable var1) {
         var1.printStackTrace();
      }

      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
         public boolean verify(String s, SSLSession sslSession) {
            return true;
         }
      });
   }
}
