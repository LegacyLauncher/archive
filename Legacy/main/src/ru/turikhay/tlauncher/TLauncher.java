package ru.turikhay.tlauncher;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.*;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.configuration.*;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.managers.*;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.frames.FirstRunNotice;
import ru.turikhay.tlauncher.ui.frames.NewFolderFrame;
import ru.turikhay.tlauncher.ui.listener.MinecraftUIListener;
import ru.turikhay.tlauncher.ui.listener.RequiredUpdateListener;
import ru.turikhay.tlauncher.ui.listener.VersionManagerUIListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.logger.Logger;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.updater.Stats;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.util.*;
import ru.turikhay.util.async.RunnableThread;
import ru.turikhay.util.stream.MirroredLinkedOutputStringStream;
import ru.turikhay.util.stream.PrintLogger;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TLauncher {
    private static final String VERSION_STRING = "1.79.11";

    private static TLauncher instance;
    private static String[] sargs;
    private static File directory;
    private static PrintLogger print;
    private static Logger logger;
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
        args = set;
        gson = new Gson();
        U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_CONFIGURATION);
        settings = Configuration.createConfiguration(set);
        useSystemLookAndFeel &= settings.getBoolean("gui.systemlookandfeel");
        TLauncherFrame.setFontSize(settings.getFloat("gui.font"));
        reloadLocale();
        if (useSystemLookAndFeel) {
            U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_LOOKANDFEEL);
            useSystemLookAndFeel = SwingUtil.initLookAndFeel();
        }

        settings.set("gui.systemlookandfeel", useSystemLookAndFeel, false);
        U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_LOGGER);
        logger = new Logger(settings, print, lang.get("logger"), settings.getLoggerType() == Configuration.LoggerType.GLOBAL);
        logger.setCloseAction(Logger.CloseAction.KILL);
        logger.frame.bottom.folder.setEnabled(true);
        Logger.updateLocale();

        if (args.has("help"))
            U.log("Help with arguments:\n", ArgumentParser.getHelp());

        if (settings.isFirstRun()) {
            U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_FIRSTRUN);

            new FirstRunNotice().showAndWait();

            File currentDir = MinecraftUtil.getWorkingDirectory(false);
            U.log("Current dir:", currentDir);
            if (NewFolderFrame.shouldWeMoveFrom(currentDir)) {
                currentDir = NewFolderFrame.selectDestination();
                if (currentDir != null) {
                    new NewFolderFrame(this, currentDir).showAndWait();
                }
            }
        }

        U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_MANAGERS);
        manager = new ComponentManager(this);
        settings.set("minecraft.gamedir", MinecraftUtil.getWorkingDirectory(), false);
        elyManager = manager.loadComponent(ElyManager.class);
        versionManager = manager.loadComponent(VersionManager.class);
        profileManager = manager.loadComponent(ProfileManager.class);
        manager.loadComponent(ComponentManagerListenerHelper.class);
        init();

        ready = true;
        U.log("Started! (" + Time.stop(this) + " ms.)");
        U.setLoadingStep(Bootstrapper.LoadingStep.SUCCESS);

        showTranslationContributors();
    }

    private void init() {
        downloader = new Downloader();
        minecraftListener = new MinecraftUIListener(this);
        vmListener = new VersionManagerUIListener(this);
        updater = new Updater();
        updater.addListener(elyManager);
        updateListener = new RequiredUpdateListener(updater);
        U.setLoadingStep(Bootstrapper.LoadingStep.LOADING_WINDOW);
        frame = new TLauncherFrame(this);
        LoginForm lf = frame.mp.defaultScene.loginForm;
        U.setLoadingStep(Bootstrapper.LoadingStep.REFRESHING_INFO);
        if (lf.autologin.isEnabled()) {
            versionManager.startRefresh(true);
            lf.autologin.setActive(true);
        } else {
            versionManager.asyncRefresh();
        }

        profileManager.refresh();

        new RunnableThread("Beacon", new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Stats.beacon();
                    updater.asyncFindUpdate();

                    try {
                        TimeUnit.MINUTES.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();

        /*Locale[] localeArray = Locale.getAvailableLocales();
        for(Locale locale : localeArray) {
            U.log(locale.toLanguageTag(), locale.getDisplayCountry(Locale.ENGLISH), locale.getDisplayLanguage(Locale.ENGLISH));
        }*/
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public LangConfiguration getLang() {
        return lang;
    }

    public Configuration getSettings() {
        return settings;
    }

    public Updater getUpdater() {
        return updater;
    }

    public OptionSet getArguments() {
        return args;
    }

    public TLauncherFrame getFrame() {
        return frame;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Gson getGson() {
        return gson;
    }

    public ComponentManager getManager() {
        return manager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public ElyManager getElyManager() {
        return elyManager;
    }

    public MinecraftLauncher getLauncher() {
        return launcher;
    }

    public MinecraftUIListener getMinecraftListener() {
        return minecraftListener;
    }

    public boolean isReady() {
        return ready;
    }

    public void reloadLocale() throws IOException {
        Locale locale = settings.getLocale();
        U.log("Selected locale: " + locale);
        if (lang == null) {
            lang = new LangConfiguration(settings.getLocales(), locale);
        } else {
            lang.setSelected(locale);
        }

        Localizable.setLang(lang);
        Alert.prepareLocal();

        if (minecraftListener != null) {
            minecraftListener.updateLocale();
        }

        if (logger != null) {
            logger.setName(lang.get("logger"));
        }
    }

    private JsonObject contributors;
    private SimpleConfiguration proofreaders;

    public void showTranslationContributors() {
        try {
            if (contributors == null) {
                contributors = (JsonObject) new JsonParser().parse(new InputStreamReader(getClass().getResourceAsStream("/lang/_contrib.json"), FileUtil.getCharset()));
            }
            if (proofreaders == null) {
                proofreaders = new SimpleConfiguration(getClass().getResource("/lang/_proofr.properties"));
            }

            Locale locale = settings.getLocale();

            boolean hasUpgraded = TLauncher.getVersion().greaterThanOrEqualTo(settings.getVersion("update.asked")),
                    contributorsHaveBeenShownBefore = locale.toString().equals(settings.get("contributors"));

            settings.set("contributors", locale.toString());
            settings.set("update.asked", TLauncher.getVersion());

            if (hasUpgraded && !contributorsHaveBeenShownBefore) {
                Locale ruLocale = U.getLocale("ru_RU");
                boolean isUssr = ruLocale != null && settings.isUSSRLocale();

                List<String> contributorList = new ArrayList<String>();
                int others = 0;

                JsonArray contribArray = (JsonArray) contributors.get(locale.toString());

                if (contribArray == null) {
                    U.log("No contributors found", locale);
                    return;
                }

                for (JsonElement elem : contribArray.getAsJsonArray()) {
                    if (elem.getAsJsonPrimitive().isNumber()) {
                        others = elem.getAsInt();
                        break;
                    }
                    contributorList.add(elem.getAsString());
                }

                if (contributorList.isEmpty()) {
                    U.log("Contributor list for", locale, "is empty");
                    return;
                }

                StringBuilder b = new StringBuilder();

                b.append("<b>TLauncher</b> ");
                b.append(isUssr ? "переведён на" : "is translated to");
                b.append(" <b>");
                b.append(locale.getDisplayName(locale));
                b.append("</b> ");
                b.append(isUssr ? "благодаря" : "thanks to");
                b.append(" ");

                if (contributorList.size() > 1) {
                    b.append(isUssr ? "этим людям" : "these people");
                    b.append(":\n");
                    for (String contributor : contributorList) {
                        b.append("\n\u2022 <b>").append(contributor).append("</b>");
                    }
                    if (others > 0) {
                        b.append("\n ... and ").append(others).append(" others");
                    }
                } else {
                    b.append("<b>").append(contributorList.get(0)).append("</b>!");
                }

                String proofreader = proofreaders.get(locale.toString());

                if (proofreader != null) {
                    b.append("\n\n");
                    b.append(isUssr ? "Перед выпуском перевод проверил товарищ" : "Translation proofreading is done by");
                    b.append(" <b>");
                    b.append(proofreader);
                    b.append("</b>");
                }

                Alert.showMessage(isUssr ? "Перевод" : "Translation", b.toString());
            }
        } catch (Exception e) {
            U.log(e);
        }
    }

    public void launch(MinecraftListener listener, ServerList.Server server, boolean forceupdate) {
        launcher = new MinecraftLauncher(this, forceupdate);
        launcher.addListener(minecraftListener);
        launcher.addListener(listener);
        launcher.addListener(frame.mp.getProgress());
        launcher.setServer(server);
        launcher.start();
    }

    public boolean isLauncherWorking() {
        return launcher != null && launcher.isWorking();
    }

    public static void kill() {
        if (TLauncher.getInstance() != null) {
            try {
                TLauncher.getInstance().getSettings().save();
            } catch (Exception e) {
                Alert.showError("Configuration error", "Could not save settings – this is not good. Please contact support if you want to solve this.", e);
            }
        }
        U.log("Good bye!");
        System.exit(0);
    }

    public void hide() {
        if (frame != null) {
            boolean doAgain = true;

            while (doAgain) {
                try {
                    frame.setVisible(false);
                    doAgain = false;
                } catch (Exception var3) {
                }
            }
        }

        U.log("I'm hiding!");
    }

    public void show() {
        if (frame != null) {
            boolean doAgain = true;

            while (doAgain) {
                try {
                    frame.setVisible(true);
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
                if (TLauncher.logger == null) {
                    try {
                        getMirror().flush();
                    } catch (IOException ioE) {
                        // ignore
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
        } catch (Throwable var10) {
            Throwable e = var10;
            U.log("Error launching TLauncher:");
            var10.printStackTrace(print);
            StackTraceElement[] var7;
            int var6 = (var7 = var10.getStackTrace()).length;

            for (int var5 = 0; var5 < var6; ++var5) {
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
                        } catch (Throwable var9) {
                            e = var9;
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
        U.log("Starting TLauncher", getBrand(), getVersion());
        U.log("Beta:", isBeta());
        U.log("Machine info:", OS.getSummary());
        U.log("Startup time:", DateFormat.getDateTimeInstance(3, 1).format(new Date()));
        U.log("Directory:", new File(""));
        U.log("Executable location:", FileUtil.getRunningJar());
        U.log("---");
        sargs = args;
        new TLauncher(ArgumentParser.parseArgs(args));
    }

    private static final Version VERSION;
    private static final boolean BETA;

    static {
        URL metaUrl = TLauncher.class.getResource("/meta.json");
        JsonObject meta;

        try {
            meta = new JsonParser().parse(new InputStreamReader(metaUrl.openStream())).getAsJsonObject();
        } catch(IOException ioE) {
            throw new Error("could not load meta", ioE);
        }

        Version genVersion = Version.valueOf(VERSION_STRING),
                version = Version.valueOf(meta.get("version").getAsString());

        if(!genVersion.equals(version)) {
            throw new Error("could not verify version");
        }

        VERSION = version;
        BETA = !VERSION.getBuildMetadata().startsWith("master");
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

    public static Version getVersion() {
        return VERSION;
    }

    public static boolean isBeta() {
        return BETA;
    }

    public boolean getDebug() {
        return args.has("debug");
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
        return Static.getUpdateRepo(); //isBeta() ? Static.getBetaUpdateRepo() : Static.getUpdateRepo();
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
            context.init(null, new X509TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Throwable t) {
            t.printStackTrace();
        }

        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });

        System.setProperty("java.net.useSystemProxies", "true");
    }
}
