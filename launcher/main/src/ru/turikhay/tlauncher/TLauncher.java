package ru.turikhay.tlauncher;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.bridge.BootEventDispatcher;
import ru.turikhay.tlauncher.configuration.*;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.managers.*;
import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.sentry.SentryBreadcrumb;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.frames.FirstRunNotice;
import ru.turikhay.tlauncher.ui.frames.NewFolderFrame;
import ru.turikhay.tlauncher.ui.listener.UIListeners;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.logger.Logger;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.updater.Stats;
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
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class TLauncher {
    private static final String VERSION = "1.79.30";

    private final boolean debug, ready;

    private final BootBridge bridge;
    private final BootEventDispatcher dispatcher;
    private final BootConfiguration bootConfig;

    private final Configuration config;
    private final LangConfiguration lang;

    private final Logger logger;

    private final ComponentManager componentManager;
    private final ElyManager elyManager;
    private final VersionManager versionManager;
    private final ProfileManager profileManager;

    private final Downloader downloader;

    private final UIListeners uiListeners;

    private final TLauncherFrame frame;

    private TLauncher(BootBridge bridge, BootEventDispatcher dispatcher) throws Exception {
        U.requireNotNull(bridge, "bridge");
        checkNotRunning();
        instance = this;

        Object timer = new Object();
        Time.start(timer);

        this.bridge = bridge;
        this.dispatcher = dispatcher;
        U.log("Options:", bridge.getOptions());

        this.bootConfig = BootConfiguration.parse(bridge);

        Repository.updateList(bootConfig.getRepositories());
        Stats.setAllowed(bootConfig.isStatsAllowed());

        OptionSet optionSet = ArgumentParser.parseArgs(bridge.getArgs());
        debug = optionSet.has("debug");

        dispatcher.onBootStateChanged("Loading configuration", 0.1);
        this.config = Configuration.createConfiguration(optionSet);
        this.lang = new LangConfiguration();
        initConfig();

        dispatcher.onBootStateChanged("Loading logger", 0.15);
        this.logger = new Logger(config, printLogger, "Logger", config.getLoggerType() == Configuration.LoggerType.GLOBAL);
        initLogger();

        dispatcher.onBootStateChanged("Handling run conditions", 0.17);
        handleWorkdir();

        dispatcher.onBootStateChanged("Preparing managers", 0.2);
        this.componentManager = new ComponentManager(this);

        dispatcher.onBootStateChanged("Loading Ely manager", 0.22);
        elyManager = componentManager.loadComponent(ElyManager.class);

        dispatcher.onBootStateChanged("Loading Version manager", 0.27);
        versionManager = componentManager.loadComponent(VersionManager.class);

        dispatcher.onBootStateChanged("Loading Profile manager", 0.35);
        profileManager = componentManager.loadComponent(ProfileManager.class);

        dispatcher.onBootStateChanged("Loading manager listener", 0.36);
        componentManager.loadComponent(ComponentManagerListenerHelper.class);

        dispatcher.onBootStateChanged("Loading Downloader", 0.4);
        downloader = new Downloader();

        dispatcher.onBootStateChanged("Loading UI Listeners", 0.5);
        uiListeners = new UIListeners(this);

        dispatcher.onBootStateChanged("Loading frame", 0.65);
        frame = new TLauncherFrame(this);

        dispatcher.onBootStateChanged("Post-init UI", 0.8);
        initAndRefreshUI();

        ready = true;

        dispatcher.onBootSucceeded();
        SentryBreadcrumb.info(TLauncher.class, "started").data("debug", debug).data("bootstrap", bridge.getBootstrapVersion()).data("delta", Time.stop(timer)).push();
    }

    public BootConfiguration getBootConfig() {
        return bootConfig;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isReady() {
        return ready;
    }

    public Configuration getSettings() {
        return config;
    }

    public LangConfiguration getLang() {
        return lang;
    }

    public Logger getLogger() {
        return logger;
    }

    public ComponentManager getManager() {
        return componentManager;
    }

    public ElyManager getElyManager() {
        return elyManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public UIListeners getUIListeners() {
        return uiListeners;
    }

    public TLauncherFrame getFrame() {
        return frame;
    }

    private MinecraftLauncher launcher;

    public MinecraftLauncher getMinecraftLauncher() {
        return launcher;
    }

    public boolean isMinecraftLauncherWorking() {
        return launcher != null && launcher.isWorking();
    }

    public MinecraftLauncher newMinecraftLauncher(String versionName, Server server, boolean forceUpdate) {
        if(isMinecraftLauncherWorking()) {
            throw new IllegalStateException("launcher is working");
        }

        launcher = new MinecraftLauncher(this, forceUpdate);

        for(MinecraftListener l : uiListeners.getMinecraftListeners()) {
            launcher.addListener(l);
        }

        launcher.setVersion(versionName);
        launcher.setServer(server);

        launcher.start();

        return launcher;
    }

    public static void kill() {
        if (TLauncher.getInstance() != null) {
            try {
                TLauncher.getInstance().getSettings().save();
            } catch (Exception e) {
                Alert.showError("Configuration error", "Could not save settings – this is not good. Please contact support if you want to solve this.", e);
            }
            U.log("Good bye!");
            TLauncher.getInstance().dispatcher.requestClose();
        } else {
            System.exit(0);
        }
    }

    private void initConfig() {
        config.setUsingSystemLookAndFeel(config.isUsingSystemLookAndFeel() && SwingUtil.initLookAndFeel());
        TLauncherFrame.setFontSize(config.getFontSize());
        reloadLocale();
    }

    private void initLogger() {
        logger.setCloseAction(Logger.CloseAction.KILL);
        logger.frame.bottom.folder.setEnabled(true);
        Logger.updateLocale();

        U.log("Logger initialized");
    }

    private void handleWorkdir() {
        if (config.isFirstRun()) {
            handleFirstRun();
        } else {
            config.set("minecraft.gamedir", MinecraftUtil.getWorkingDirectory(), false);
        }
    }

    private void handleFirstRun() {
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

    private void initAndRefreshUI() {
        LoginForm lf = frame.mp.defaultScene.loginForm;

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

                    try {
                        TimeUnit.MINUTES.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public void reloadLocale() {
        Locale locale = config.getLocale();

        U.log("Selected locale:", locale);
        lang.setLocale(locale);

        Localizable.setLang(lang);
        Alert.prepareLocal();

        if(uiListeners != null) {
            uiListeners.updateLocale();
        }

        if (logger != null) {
            logger.setName(lang.get("logger"));
        }
    }

    private TLauncher() {
        throw new RuntimeException("should not be called");
    }

    private static TLauncher instance;
    private static final Version SEMVER;
    private static final boolean BETA;

    private static PrintLogger printLogger;

    public static TLauncher getInstance() {
        return instance;
    }

    public static Version getVersion() {
        return SEMVER;
    }

    public static boolean isBeta() {
        return BETA;
    }

    static {
        URL metaUrl = TLauncher.class.getResource("meta.json");
        JsonObject meta;

        try {
            meta = new JsonParser().parse(new InputStreamReader(metaUrl.openStream())).getAsJsonObject();
        } catch (IOException ioE) {
            throw new Error("could not load meta", ioE);
        }

        Version genVersion = Version.valueOf(VERSION),
                version = Version.valueOf(meta.get("version").getAsString());

        if (!genVersion.equals(version)) {
            throw new Error("could not verify version; expected: " + genVersion + ", got: " + version);
        }

        SEMVER = version;
        BETA = SEMVER.getBuildMetadata() != null && !SEMVER.getBuildMetadata().startsWith("master");
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

    public static void launch(BootBridge bridge) {
        checkNotRunning();

        setupErrorHandler();

        U.setPrefix(">>");
        setupPrintStream();

        U.log("Starting TLauncher", getBrand(), getVersion());
        if(bridge.getBootstrapVersion() != null) {
            U.log("... from Bootstrap", bridge.getBootstrapVersion());
        }
        U.log("Beta:", isBeta());
        U.log("Machine info:", OS.getSummary());
        U.log("Startup time:", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(new Date()));
        U.log("Directory:", new File(""));
        U.log("Executable location:", FileUtil.getRunningJar());
        U.log("---");

        BootEventDispatcher dispatcher = bridge.setupDispatcher();
        dispatcher.onBootStarted();

        while(true) {
            try {
                new TLauncher(bridge, dispatcher);
            } catch (Throwable t) {
                U.log("Error launcing TLauncher:", t);

                if(handleLookAndFeelException(t)) {
                    continue;
                }

                dispatcher.onBootErrored(t);

                return;
            }
            break;
        }
    }

    public static void main(String[] args) {
        launch(BootBridge.create(args));
    }

    private static void checkNotRunning() {
        if (instance != null) {
            throw new IllegalStateException("already running");
        }
    }

    private static void setupErrorHandler() {
        ExceptionHandler handler = ExceptionHandler.getInstance();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Thread.currentThread().setUncaughtExceptionHandler(handler);
    }

    private static void setupPrintStream() {
        MirroredLinkedOutputStringStream stream = new MirroredLinkedOutputStringStream() {
            public void flush() {
                if (TLauncher.getInstance() == null || TLauncher.getInstance().getLogger() == null) {
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
        printLogger = new PrintLogger(stream);
        stream.setLogger(printLogger);
        System.setOut(printLogger);
    }

    private static boolean handleLookAndFeelException(Throwable t) {
        for(StackTraceElement elem : t.getStackTrace()) {
            if (elem.toString().toLowerCase().contains("lookandfeel")) {
                SwingUtil.resetLookAndFeel();
                return true;
            }
        }
        return false;
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
