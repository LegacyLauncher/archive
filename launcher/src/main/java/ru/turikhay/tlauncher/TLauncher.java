package ru.turikhay.tlauncher;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import io.sentry.event.interfaces.ExceptionInterface;
import joptsimple.OptionSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.bridge.BootEventDispatcher;
import ru.turikhay.tlauncher.bootstrap.bridge.BootMessage;
import ru.turikhay.tlauncher.configuration.*;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.handlers.ExceptionHandler;
import ru.turikhay.tlauncher.logger.Log4j2ContextHelper;
import ru.turikhay.tlauncher.logger.LoggerBuffer;
import ru.turikhay.tlauncher.logger.LoggerInterface;
import ru.turikhay.tlauncher.logger.SwingLoggerAppender;
import ru.turikhay.tlauncher.managers.*;
import ru.turikhay.tlauncher.minecraft.PromotedServer;
import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.sentry.SentryConfigurer;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.frames.FirstRunNotice;
import ru.turikhay.tlauncher.ui.frames.NewFolderFrame;
import ru.turikhay.tlauncher.ui.frames.UpdateFrame;
import ru.turikhay.tlauncher.ui.listener.UIListeners;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.logger.SwingLogger;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.*;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.async.RunnableThread;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class TLauncher {
    private static final Logger LOGGER = LogManager.getLogger(TLauncher.class);

    private final boolean debug, ready;

    private final BootBridge bridge;
    private final BootEventDispatcher dispatcher;
    private final BootConfiguration bootConfig;

    private final Configuration config;
    private final LangConfiguration lang;

    private SwingLogger loggerUI;

    private final ComponentManager componentManager;
    private final LibraryReplaceProcessor libraryReplaceManager;
    private final VersionManager versionManager;
    private final ProfileManager profileManager;
    private final JavaManager javaManager;
    private final MigrationManager migrationManager;

    private final Downloader downloader;

    private final UIListeners uiListeners;

    private TLauncherFrame frame;

    private long sessionStartTime;;

    private TLauncher(BootBridge bridge, BootEventDispatcher dispatcher) throws Exception {
        U.requireNotNull(bridge, "bridge");
        checkNotRunning();
        instance = this;

        Object timer = new Object();
        Time.start(timer);

        this.bridge = bridge;
        this.dispatcher = dispatcher;
        LOGGER.debug("Options: {}", bridge.getOptions() == null? null : bridge.getOptions().length() + " code units");

        OptionSet optionSet = ArgumentParser.parseArgs(bridge.getArgs());
        debug = optionSet.has("debug");

        dispatcher.onBootStateChanged("Loading configuration", 0.1);
        this.config = Configuration.createConfiguration(optionSet);
        dispatcher.passClient(config.getClient());
        this.lang = new LangConfiguration();
        initConfig();

        SwingUtil.wait(this::reloadLoggerUI);

        this.bootConfig = BootConfiguration.parse(bridge);

        Repository.updateList(bootConfig.getRepositories());
        Stats.setAllowed(bootConfig.isStatsAllowed());

        dispatcher.onBootStateChanged("Handling run conditions", 0.17);
        handleWorkdir();
        if (!config.isFirstRun())
            handleUpdate();
        handleNoticeHiding();

        dispatcher.onBootStateChanged("Preparing managers", 0.2);
        this.componentManager = new ComponentManager(this);

        dispatcher.onBootStateChanged("Loading Library Replace manager", 0.22);
        libraryReplaceManager = componentManager.loadComponent(LibraryReplaceProcessor.class);
        libraryReplaceManager.setAllowElyEverywhere(bootConfig.isElyAllowed());

        dispatcher.onBootStateChanged("Loading Version manager", 0.27);
        versionManager = componentManager.loadComponent(VersionManager.class);

        dispatcher.onBootStateChanged("Loading Profile manager", 0.35);
        profileManager = componentManager.loadComponent(ProfileManager.class);

        migrateFromOldJreConfig();
        File jreRootDir = new File(config.get(JavaManagerConfig.class).getRootDirOrDefault());
        FileUtil.createFolder(jreRootDir);
        javaManager = new JavaManager(jreRootDir);

        migrationManager = new MigrationManager(this);

        dispatcher.onBootStateChanged("Loading manager listener", 0.36);
        componentManager.loadComponent(ComponentManagerListenerHelper.class);

        dispatcher.onBootStateChanged("Loading Downloader", 0.4);
        downloader = new Downloader();

        dispatcher.onBootStateChanged("Loading UI Listeners", 0.5);
        uiListeners = new UIListeners(this);

        SwingUtil.wait(() -> {
            dispatcher.onBootStateChanged("Loading frame", 0.65);
            frame = new TLauncherFrame(this);

            dispatcher.onBootStateChanged("Post-init UI", 0.8);
            initAndRefreshUI();
        });

        ready = true;
        sessionStartTime = System.currentTimeMillis();

        dispatcher.onBootSucceeded();

        if(config.getClient().toString().equals("23a9e755-046a-4250-9e03-1920baa98aeb")) {
            config.set("client", UUID.randomUUID());
            String creationTime = "unknown";
            try {
                creationTime = Files.readAttributes(config.getFile().toPath(), BasicFileAttributes.class).creationTime().toString();
            } catch(Exception e) {
                // ignore
            }
        }

        final int testIteration = 1;
        if(config.getInteger("connection.testPassed") != testIteration) {
            AsyncThread.execute(new Runnable() {
                @Override
                public void run() {
                    Set<String> urlList = new LinkedHashSet<>();
                    urlList.add("https://s3.amazonaws.com/Minecraft.Download/versions/versions.json");
                    for (String extraRepo : Static.getExtraRepo()) {
                        urlList.add(extraRepo + "versions/versions.json");
                    }
                    urlList.add("https://account.ely.by/");
                    urlList.add("https://tlauncher.ru/test.txt");
                    urlList.add("https://launchermeta.mojang.com/mc/game/version_manifest.json");
                    for (String _url : urlList) {
                        URL url = null;
                        String response;
                        try {
                            url = new URL(_url);
                            response = IOUtils.toString(url, StandardCharsets.UTF_8);
                            if (_url.endsWith("json") && !response.startsWith("{")) {
                                throw new IOException("invalid json response");
                            }
                            LOGGER.debug("Connection OK: {}", _url);
                        } catch (Exception e) {
                            Sentry.capture(new EventBuilder()
                                    .withLevel(Event.Level.WARNING)
                                    .withMessage("test connection failed: " + _url)
                                    .withSentryInterface(new ExceptionInterface(e))
                                    .withExtra("ip", U.resolveHost(url))
                            );
                            LOGGER.warn("Test connection to {} failed", _url, e);
                        }
                    }
                    config.set("connection.testPassed", testIteration);
                }
            });
        }

        preloadUI();

        migrationManager.queueMigrationCheck();
    }

    private void migrateFromOldJreConfig() {
        String cmd = config.get("minecraft.cmd");
        if(cmd == null) {
            return;
        }

        LOGGER.info("Migrating from old JRE configuration");
        LOGGER.info("minecraft.cmd -> {} = {}", JavaManagerConfig.Custom.PATH_CUSTOM_PATH, cmd);
        LOGGER.info("{} = {}", JavaManagerConfig.PATH_JRE_TYPE, JavaManagerConfig.Custom.TYPE);

        config.set("minecraft.cmd", null);
        config.set(JavaManagerConfig.PATH_JRE_TYPE, JavaManagerConfig.Custom.TYPE);
        config.set(JavaManagerConfig.Custom.PATH_CUSTOM_PATH, cmd);
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

    public ComponentManager getManager() {
        return componentManager;
    }

    public LibraryReplaceProcessor getLibraryManager() {
        return libraryReplaceManager;
    }

    public VersionManager getVersionManager() {
        return versionManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public JavaManager getJavaManager() {
        return javaManager;
    }

    public MigrationManager getMigrationManager() {
        return migrationManager;
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

    public MinecraftLauncher newMinecraftLauncher(String versionName, Server server, int serverId, boolean forceUpdate) {
        if(isMinecraftLauncherWorking()) {
            throw new IllegalStateException("launcher is working");
        }

        launcher = new MinecraftLauncher(this, forceUpdate);

        for(MinecraftListener l : uiListeners.getMinecraftListeners()) {
            launcher.addListener(l);
        }

        launcher.setVersion(versionName);
        launcher.setServer(server, serverId);

        List<PromotedServer> promotedServerList = new ArrayList<>();
        if(bootConfig.getPromotedServers().containsKey(getSettings().getLocale().toString())) {
            promotedServerList.addAll(bootConfig.getPromotedServers().get(getSettings().getLocale().toString()));
        } else if (bootConfig.getPromotedServers().containsKey("global")) {
            promotedServerList.addAll(bootConfig.getPromotedServers().get("global"));
        }

        List<PromotedServer> outdatedServerList = new ArrayList<>();
        if(bootConfig.getOutdatedPromotedServers().containsKey(getSettings().getLocale().toString())) {
            outdatedServerList.addAll(bootConfig.getOutdatedPromotedServers().get(getSettings().getLocale().toString()));
        } else if (bootConfig.getOutdatedPromotedServers().containsKey("global")) {
            outdatedServerList.addAll(bootConfig.getOutdatedPromotedServers().get("global"));
        }
        launcher.setPromotedServers(promotedServerList, outdatedServerList);

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
            LOGGER.info("Goodbye!");
            // "close" main window
            TLauncher.getInstance().frame.setVisible(false);
            // report and wait 5 seconds
            try {
                Stats.reportSessionDuration(getInstance().sessionStartTime).get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            }
            TLauncher.getInstance().dispatcher.requestClose();
        } else {
            System.exit(0);
        }
    }

    private void initConfig() {
        SentryConfigurer.setUser(config.getClient());
        config.setUsingSystemLookAndFeel(config.isUsingSystemLookAndFeel() && SwingUtil.initLookAndFeel());
        TLauncherFrame.setFontSize(config.getFontSize());
        if(!config.getBoolean("connection.ssl")) {
            LOGGER.info("Disabling SSL certificate/hostname validation");
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
            } catch(Exception e) {
                LOGGER.error("Could not init SSLContext", e);
            }
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        }
        reloadLocale();
    }

    public void reloadLoggerUI() {
        SwingLoggerAppender swingLoggerAppender = Log4j2ContextHelper.getSwingLoggerAppender();
        LoggerInterface li = swingLoggerAppender.getLoggerInterface();
        boolean enableLogger = config.getLoggerType() == Configuration.LoggerType.GLOBAL;
        if(loggerUI == null) {
            if(!enableLogger) {
                return;
            }
            loggerUI = new SwingLogger(config);
            if(li instanceof LoggerBuffer) {
                loggerUI.drainFrom((LoggerBuffer) li);
            }
            swingLoggerAppender.setLoggerInterface(loggerUI);
            initLoggerUI();
            LOGGER.debug("Logger initialized");
        } else {
            if(enableLogger) {
                return;
            }
            swingLoggerAppender.setLoggerInterface(new LoggerBuffer());
            loggerUI.dispose();
            loggerUI = null;
            LOGGER.debug("Logger disposed");
        }
    }

    private void initLoggerUI() {
        loggerUI.setFolderAction(() -> {
            if(isMinecraftLauncherWorking()) {
                OS.openFolder(getMinecraftLauncher().getGameDir());
            } else {
                OS.openFolder(MinecraftUtil.getWorkingDirectory());
            }
        });
        loggerUI.setSaveAction(() -> {
            OS.openFile(Log4j2ContextHelper.getCurrentLogFile().getFile());
        });
        updateLoggerUIActions();
        loggerUI.show();
    }

    public void updateLoggerUIActions() {
        if(loggerUI == null) {
            return;
        }
        if(isMinecraftLauncherWorking() && getMinecraftLauncher().isMinecraftRunning()) {
            loggerUI.setKillAction(() -> getMinecraftLauncher().killProcess());
        } else {
            loggerUI.setKillAction(null);
        }
    }

    private void handleNoticeHiding() {
        if(!isNoticeDisablingAllowed()) {
            config.set("notice.enabled", true);
        }
    }

    private void handleWorkdir() {
        if (config.isFirstRun()) {
            handleFirstRun();
        } else {
            config.set("minecraft.gamedir", MinecraftUtil.getWorkingDirectory(), false);
        }
    }

    private void handleUpdate() {
        String locale = getSettings().getLocale().toString();
        boolean isRussian = locale.equals("ru_RU");
        BootMessage message = dispatcher.getBootMessage(locale);
        if (message == null) {
            message = dispatcher.getBootMessage("en_US");
        }
        if (message != null) {
            new UpdateFrame(U.getMinorVersion(getVersion()), message.getBody()).showAndWait();
        }
    }

    private void handleFirstRun() {
        new FirstRunNotice().showAndWait();

        File currentDir = MinecraftUtil.getWorkingDirectory(false);
        LOGGER.info("Current dir: {}", currentDir);
        if (NewFolderFrame.shouldWeMoveFrom(currentDir)) {
            currentDir = NewFolderFrame.selectDestination();
            if (currentDir != null) {
                new NewFolderFrame(this, currentDir).showAndWait();
            }
        }
    }

    public boolean isNoticeDisablingAllowed() {
        return bootConfig.isAllowNoticeDisable(config.getClient());
    }

    public boolean isNoticeDisabled() {
        return isNoticeDisablingAllowed() && !config.getBoolean("notice.enabled");
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
                Stats.submitNoticeStatus(config.getBoolean("notice.enabled"));
                while (true) {
                    try {
                        TimeUnit.MINUTES.sleep(30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Stats.beacon();
                }
            }
        }).start();
    }

    private void preloadUI() {
        frame.mp.defaultScene.settingsForm.load();
    }

    public void reloadLocale() {
        Locale locale = config.getLocale();

        LOGGER.info("Selected locale: {}", locale);
        lang.setLocale(locale);

        SwingUtil.wait(() -> {
            Localizable.setLang(lang);
            Alert.prepareLocal();

            if (loggerUI != null) {
                loggerUI.updateLocale();
            }

            if (uiListeners != null) {
                uiListeners.updateLocale();
            }

            if(migrationManager != null && migrationManager.getFrame() != null) {
                migrationManager.getFrame().updateLocale();
            }
        });
    }

    private TLauncher() {
        throw new RuntimeException("should not be called");
    }

    private static TLauncher instance;
    private static final Version SEMVER;
    private static final boolean BETA;

    public static TLauncher getInstance() {
        return instance;
    }

    public static Version getVersion() {
        return SEMVER;
    }

    public String getBootstrapVersion() {
        return bridge.getBootstrapVersion();
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

        SEMVER = U.requireNotNull(Version.valueOf(meta.get("version").getAsString()), "semver");
        BETA = !StringUtils.isBlank(SEMVER.getBuildMetadata());
    }

    public static String getBrand() {
        return Static.getBrand();
    }

    public static String getShortBrand() {
        return Static.getShortBrand();
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

    public static String getSupportEmail() {
        return "support@tlauncher.ru";
    }

    public static void launch(BootBridge bridge) throws InterruptedException {
        checkNotRunning();

        setupErrorHandler();

        LOGGER.info("Starting TL {} {}", getBrand(), getVersion());
        if (bridge.getBootstrapVersion() != null) {
            LOGGER.info("... using Bootstrap {}", bridge.getBootstrapVersion());
        }
        LOGGER.info("Beta: {}", isBeta());
        LOGGER.info("Machine info: {}", OS.getSummary());
        LOGGER.info("Java version: {}", OS.JAVA_VERSION);
        LOGGER.info("Startup time: {}", Instant.now());
        LOGGER.info("Directory: {}", new File(""));
        LOGGER.info("Executable location: {}", FileUtil.getRunningJar());
        LOGGER.info("---");

        BootEventDispatcher dispatcher = bridge.setupDispatcher();
        dispatcher.onBootStarted();

        while (true) {
            try {
                new TLauncher(bridge, dispatcher);
                break;
            } catch (Throwable t) {
                LOGGER.fatal("Error launching TL", t);

                if (!handleLookAndFeelException(t)) {
                    dispatcher.onBootErrored(t);
                    return;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
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

    private static boolean handleLookAndFeelException(Throwable t) {
        for(StackTraceElement elem : t.getStackTrace()) {
            if (elem.toString().toLowerCase(java.util.Locale.ROOT).contains("lookandfeel")) {
                SwingUtil.resetLookAndFeel();
                return true;
            }
        }
        return false;
    }

    static {
        System.setProperty("java.net.useSystemProxies", "true");
        SentryConfigurer.configure(getVersion(), getShortBrand());
    }
}
