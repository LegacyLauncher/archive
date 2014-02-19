package com.turikhay.tlauncher;

import java.io.IOException;
import java.util.Locale;

import joptsimple.OptionSet;
import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.component.managers.ComponentManager;
import com.turikhay.tlauncher.component.managers.ProfileManager;
import com.turikhay.tlauncher.component.managers.VersionManager;
import com.turikhay.tlauncher.configuration.ArgumentParser;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.tlauncher.ui.console.Console.CloseAction;
import com.turikhay.tlauncher.ui.listeners.UpdaterUIListener;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.Time;
import com.turikhay.util.U;
import com.turikhay.util.logger.MirroredLinkedStringStream;
import com.turikhay.util.logger.PrintLogger;

public class TLauncher {
	private final static double VERSION = 0.65;
	private final static String SETTINGS = "tlauncher.cfg", BRAND = "Original";
	private final static String[] DEFAULT_UPDATE_REPO = {
        "http://u.to/tlauncher-original-update-mirror-3/D9wMBg",
        "http://s1.mmods.ru/launcher/original.ini",
        "http://u.to/tlauncher-original/BlPcBA",
        "http://ru-minecraft.org/update/original.ini",
    	"http://u.to/tlauncher-original-update/T4ASBQ",
    	"http://5.9.120.11/update/original.ini",
        "http://u.to/tlauncher-original-update-mirror2/BIQSBQ",
        "http://dl.dropboxusercontent.com/u/6204017/update/original.ini",
	};
	private final static String[]
		OFFICIAL_REPO = {"http://s3.amazonaws.com/Minecraft.Download/"}, 
		EXTRA_REPO = {
			"http://5.9.120.11/update/versions/",
			"http://s1.mmods.ru/launcher/",
			"http://ru-minecraft.org/update/tlauncher/extra/",
			"http://dl.dropboxusercontent.com/u/6204017/update/versions/"
		};
	private final static String[]
		LIBRARY_REPO = {"https://libraries.minecraft.net/"},
		ASSETS_REPO = {"http://resources.download.minecraft.net/"},
		SERVER_LIST = null;
	
	private static TLauncher instance;
	private static String[] sargs;
	
	private static MirroredLinkedStringStream stream;
	private static PrintLogger print;
	private static Console console;
	
	private TLauncherState state;
	
	private LangConfiguration lang;
	private Configuration settings;
	private Downloader downloader;
	private Updater updater;
	
	private TLauncherFrame frame;
	private TLauncherNoGraphics loader;
	
	private ComponentManager manager;
	private VersionManager versionManager;
	private ProfileManager profileManager;
	
	public final OptionSet args;
	
	private MinecraftLauncher launcher;
	
	private UpdaterUIListener updaterListener;
	
	private boolean ready;
	
	public TLauncher(TLauncherState state, OptionSet set) throws Exception {
		if(state == null)
			throw new IllegalArgumentException("TLauncherState can't be NULL!");
		
		U.log("TLauncher is loading in state", state);
		
		Time.start(this);
		instance = this; this.state = state; this.args = set;
		
		settings = Configuration.createConfiguration(set);
		reloadLocale();
		
		console = new Console(settings, print, "TLauncher Dev Console", settings.getConsoleType() == ConsoleType.GLOBAL);
		if(state.equals(TLauncherState.MINIMAL)) console.setCloseAction(CloseAction.KILL);
		Console.updateLocale();
		
		manager = new ComponentManager();
		
		versionManager = manager.getVersionManager();
		profileManager = manager.getProfileManager();
		
		init();
		
		U.log("Started! ("+Time.stop(this)+" ms.)");
		FileUtil.deleteFile(MinecraftUtil.getSystemRelatedFile("tlauncher.ini")); // TODO remove
		
		this.ready = true;
	}
	
	private void init(){
		downloader = new Downloader(this);
		
		switch(state){
		case FULL:
			updaterListener = new UpdaterUIListener(this);
			
			updater = new Updater(this);
			updater.addListener(updaterListener);
			
			frame = new TLauncherFrame(this);
			
			LoginForm lf = frame.mp.defaultScene.loginForm;
			
			if(lf.autologin.isEnabled()){
				versionManager.startRefresh(true);
				lf.autologin.setActive(true);
			} else {
				versionManager.asyncRefresh();
				updater.asyncFindUpdate();
			}
			
			profileManager.refresh();
			
			break;
		case MINIMAL:			
			loader = new TLauncherNoGraphics(this);
			break;
		}
		downloader.startLaunch();
	}
	
	public Downloader getDownloader(){ return downloader; }
	public LangConfiguration getLang(){ return lang; }
	public Configuration getSettings(){ return settings; }
	public Updater getUpdater(){ return updater; }
	
	public OptionSet getArguments(){ return args; }
	
	public TLauncherFrame getFrame(){ return frame; }
	public TLauncherNoGraphics getLoader(){ return loader; }
	
	public static Console getConsole(){ return console; }
	
	public ComponentManager getManager(){ return manager; }
	public VersionManager getVersionManager(){ return versionManager; }
	public ProfileManager getProfileManager(){ return profileManager; }
	
	public UpdaterUIListener getUpdaterListener(){ return updaterListener; }
	
	public boolean isReady(){ return ready; }
	
	public void reloadLocale() throws IOException {
		Locale locale = settings.getLocale();
		U.log("Selected locale: " + locale);
		
		if(lang == null)
			lang = new LangConfiguration(settings.getLocales(), locale);
		else
			lang.setSelected(locale);
		
		Alert.prepareLocal();
		Localizable.setLang(lang);
	}
	
	public void launch(MinecraftLauncherListener listener, boolean forceupdate) {
		this.launcher = new MinecraftLauncher(this, listener, forceupdate, true);
		launcher.start();
	}
	
	public boolean isLauncherWorking(){
		return (launcher != null) && (launcher.isWorking());
	}
	
	public static void kill(){ U.log("Good bye!"); System.exit(0); }
	public void hide(){ U.log("Hiding..."); frame.setVisible(false); }
	public void show(){ U.log("Here I am!"); frame.setVisible(true); }
	
	public static double getVersion() { return VERSION; }
	public static String getBrand(){ return BRAND; }
	public static String[] getUpdateRepos() { return DEFAULT_UPDATE_REPO; }
	public static String getSettingsFile() { return SETTINGS; }
	public static String[] getOfficialRepo(){ return OFFICIAL_REPO; }
	public static String[] getExtraRepo(){ return EXTRA_REPO; }
	public static String[] getLibraryRepo(){ return LIBRARY_REPO; }
	public static String[] getAssetsRepo(){ return ASSETS_REPO; }
	public static String[] getServerList(){ return SERVER_LIST; }
	
	/*___________________________________*/
	
	public static void main(String[] args){
		ExceptionHandler handler = ExceptionHandler.getInstance();
		Thread.setDefaultUncaughtExceptionHandler(handler);
		
		U.setPrefix("[TLauncher]");
		
		stream = new MirroredLinkedStringStream();
		stream.setMirror(System.out);
		
		print = new PrintLogger(stream);
		stream.setLogger(print);
		System.setOut(print);
		
		TLauncherFrame.initLookAndFeel();
		
		try { launch(args); }
		catch(Throwable e){
			U.log("Error launching TLauncher:");
			e.printStackTrace(print);
			
			Alert.showError(e, true);
		}
	}
	
	private static void launch(String[] args) throws Exception {		
		U.log("Hello!");		
	    U.log("Starting TLauncher", BRAND, VERSION);
	    U.log("Machine info:", OperatingSystem.getCurrentInfo());
	    
	    U.log("---");
	    
	    TLauncher.sargs = args;

	    OptionSet set = ArgumentParser.parseArgs(args);
	    if(set == null) { new TLauncher(TLauncherState.FULL, null); return; }
	    if(set.has("help")) ArgumentParser.getParser().printHelpOn(System.out);

	    TLauncherState state = TLauncherState.FULL;
	    if (set.has("nogui")) state = TLauncherState.MINIMAL;

	    new TLauncher(state, set);
	}
	
	public static String[] getArgs(){
		if(sargs == null) sargs = new String[0];
		return sargs;
	}
	
	public static TLauncher getInstance(){
		return instance;
	}
	
	public void newInstance(){
		Bootstrapper.main(sargs);
	}
	
	public enum TLauncherState {
		FULL, MINIMAL;
	}
}
