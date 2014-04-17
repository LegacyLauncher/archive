package com.turikhay.tlauncher;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import joptsimple.OptionSet;

import com.google.gson.Gson;

import com.turikhay.tlauncher.configuration.ArgumentParser;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.handlers.SimpleHostnameVerifier;
import com.turikhay.tlauncher.managers.ComponentManager;
import com.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.managers.VersionManager;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.tlauncher.ui.console.Console.CloseAction;
import com.turikhay.tlauncher.ui.listener.MinecraftUIListener;
import com.turikhay.tlauncher.ui.listener.UpdaterUIListener;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.util.OS;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import com.turikhay.util.logger.MirroredLinkedStringStream;
import com.turikhay.util.logger.PrintLogger;

public class TLauncher {
	private static final double VERSION = 0.962;

	private static TLauncher instance;
	private static String[] sargs;
	private static File directory;

	private static PrintLogger print;
	private static Console console;

	private static Gson gson;

	private TLauncherState state;

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

	private TLauncher(TLauncherState state, OptionSet set) throws Exception {
		if (state == null)
			throw new IllegalArgumentException("TLauncherState can't be NULL!");
		
		U.log("TLauncher is loading in state", state);

		Time.start(this);
		instance = this;
		this.state = state;
		this.args = set;
		
		gson = new Gson();

		settings = Configuration.createConfiguration(set);
		reloadLocale();

		console = new Console(settings, print, "TLauncher Dev Console",
				settings.getConsoleType() == ConsoleType.GLOBAL);
		if (state.equals(TLauncherState.MINIMAL))
			console.setCloseAction(CloseAction.KILL);
		Console.updateLocale();

		manager = new ComponentManager(this);

		versionManager = manager.loadComponent(VersionManager.class);
		profileManager = manager.loadComponent(ProfileManager.class);

		manager.loadComponent(ComponentManagerListenerHelper.class); // TODO invent something better

		init();

		U.log("Started! (" + Time.stop(this) + " ms.)");
		this.ready = true;
	}

	private void init() {
		downloader = new Downloader(this);
		minecraftListener = new MinecraftUIListener(this);

		switch (state) {
		case FULL:
			updaterListener = new UpdaterUIListener(this);

			updater = new Updater(this);
			updater.addListener(updaterListener);

			frame = new TLauncherFrame(this);

			LoginForm lf = frame.mp.defaultScene.loginForm;

			if (lf.autologin.isEnabled()) {
				versionManager.startRefresh(true);
				lf.autologin.setActive(true);
			} else {
				versionManager.asyncRefresh();
				updater.asyncFindUpdate();
			}

			profileManager.refresh();

			break;
		case MINIMAL:
			loader = new TLauncherLite(this);
			break;
		}
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

	public TLauncherLite getLoader() {
		return loader;
	}

	public static Console getConsole() {
		return console;
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

	public MinecraftLauncher getLauncher() {
		return launcher;
	}

	public UpdaterUIListener getUpdaterListener() {
		return updaterListener;
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

		if (lang == null)
			lang = new LangConfiguration(settings.getLocales(), locale);
		else
			lang.setSelected(locale);

		Localizable.setLang(lang);
		Alert.prepareLocal();
	}

	public void launch(MinecraftListener listener, boolean forceupdate) {
		this.launcher = new MinecraftLauncher(this, forceupdate);
		launcher.addListener(minecraftListener);
		launcher.addListener(listener);
		launcher.addListener(frame.mp.getProgress());

		launcher.start();
	}

	public boolean isLauncherWorking() {
		return launcher != null && launcher.isWorking();
	}

	public static void kill() {
		U.log("Good bye!");
		System.exit(0);
	}

	public void hide() {
		U.log("I'm hiding!");
		
		if (frame != null)
			frame.setVisible(false);
	}

	public void show() {
		U.log("Here I am!");
		
		if (frame != null)
			frame.setVisible(true);
	}

	/* ___________________________________ */

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

		TLauncherFrame.initLookAndFeel();

		try {
			launch(args);
		} catch (Throwable e) {
			U.log("Error launching TLauncher:");
			e.printStackTrace(print);

			Alert.showError(e, true);
		}
	}

	private static void launch(String[] args) throws Exception {
		U.log("Hello!");
		U.log("Starting TLauncher", BRAND, VERSION);
		U.log("Machine info:", OS.getSummary());

		U.log("---");

		TLauncher.sargs = args;

		OptionSet set = ArgumentParser.parseArgs(args);
		if (set == null) {
			new TLauncher(TLauncherState.FULL, null);
			return;
		}
		if (set.has("help"))
			ArgumentParser.getParser().printHelpOn(System.out);

		TLauncherState state = TLauncherState.FULL;
		if (set.has("nogui"))
			state = TLauncherState.MINIMAL;

		new TLauncher(state, set);
	}

	public static String[] getArgs() {
		if (sargs == null)
			sargs = new String[0];
		return sargs;
	}

	public static File getDirectory() {
		if (directory == null)
			directory = new File(".");
		return directory;
	}

	public static TLauncher getInstance() {
		return instance;
	}

	public void newInstance() {
		Bootstrapper.main(sargs);
	}

	public enum TLauncherState {
		FULL, MINIMAL
	}

	/* ___________________________________ */

	private final static String SETTINGS = "tlauncher.cfg", BRAND = "Original",
			FOLDER = "minecraft";
	private final static String[] DEFAULT_UPDATE_REPO = {
			"http://u.to/tlauncher-original-update-mirror-3/D9wMBg",
			"http://s1.mmods.ru/launcher/original.ini",
			"http://u.to/tlauncher-original/BlPcBA",
			"http://ru-minecraft.org/update/original.ini",
			"http://u.to/tlauncher-original-update/T4ASBQ",
			"http://5.9.120.11/update/original.ini",
			"http://u.to/tlauncher-original-update-mirror2/BIQSBQ",
			"http://dl.dropboxusercontent.com/u/6204017/update/original.ini" };
	private final static String[] OFFICIAL_REPO = { "http://s3.amazonaws.com/Minecraft.Download/" },
			EXTRA_REPO = { "http://5.9.120.11/update/versions/",
					"http://s1.mmods.ru/launcher/",
					"http://dl.dropboxusercontent.com/u/6204017/update/versions/" },
			FORGE_REPO = {};
	private final static String[] LIBRARY_REPO = { "https://libraries.minecraft.net/" },
			ASSETS_REPO = { "http://resources.download.minecraft.net/" },
			SERVER_LIST = {}, MOD_LIST = {};

	public static double getVersion() {
		return VERSION;
	}

	public static String getBrand() {
		return BRAND;
	}

	public static String getFolder() {
		return FOLDER;
	}

	public static String[] getUpdateRepos() {
		return DEFAULT_UPDATE_REPO;
	}

	public static String getSettingsFile() {
		return SETTINGS;
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
}
