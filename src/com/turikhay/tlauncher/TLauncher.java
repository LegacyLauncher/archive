package com.turikhay.tlauncher;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionManager;

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
import com.turikhay.tlauncher.ui.Localizable;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.util.U;

public class TLauncher {
	private final static double VERSION = 0.31;
	private final static String SETTINGS = "tlauncher.ini", BRAND = "Original";
	private final static String[] DEFAULT_UPDATE_REPO = {
        "http://u.to/tlauncher-original/BlPcBA",
        "http://ru-minecraft.org/update/original.ini",
    	"http://u.to/tlauncher-original-update/T4ASBQ",
    	"http://5.9.120.11/update/original.ini",
        "http://u.to/tlauncher-original-update-mirror2/BIQSBQ",
        "http://dl.dropboxusercontent.com/u/6204017/update/original.ini"
	};
	
	private static TLauncher instance;
	
	private TLauncherState state;
	
	private Settings lang;
	private GlobalSettings settings;
	private Downloader downloader;
	private Updater updater;
	
	private TLauncherFrame frame;
	private TLauncherNoGraphics loader;
	
	private VersionManager vm;
	private ProfileLoader pl;
	
	private final static UUID clientToken = UUID.randomUUID();
	
	public final OptionSet args;
	public final String[] sargs;
	
	public TLauncher(TLauncherState state, String[] sargs, OptionSet set) throws IOException {		
		if(state == null)
			throw new IllegalArgumentException("TLauncherState can't be NULL!");
		U.log("State:", state);
		
		instance = this; this.state = state; this.args = set; this.sargs = sargs;
		long start = System.currentTimeMillis();
		
		settings = GlobalSettings.createInstance(set); reloadLocale();
		vm = new VersionManager();
		pl = new ProfileLoader();
		
		init();
		
		long end = System.currentTimeMillis(), diff = end - start;
		U.log("Started! ("+diff+" ms.)");
	}
	
	private void init(){
		switch(state){
		case FULL:
			downloader = new Downloader(10);
			updater = new Updater(this);
			frame = new TLauncherFrame(this);
			
			final LoginForm lf = frame.getLoginForm();
			
			vm.addRefreshedListener(lf.versionchoice);
			vm.addRefreshedListener(lf.buttons.addbuttons.refresh);
			updater.addListener(lf);
			updater.addListener(frame);
			
			if(lf.autologin.isEnabled()){
				vm.refreshVersions(true);
				lf.autologin.startLogin();
			} else {
				vm.asyncRefresh();
				updater.asyncFindUpdate();
			}
			
			break;
		case MINIMAL:			
			downloader = new Downloader(1);
			loader = new TLauncherNoGraphics(this);
			break;
		}
		downloader.startLaunch();
	}
	
	public Downloader getDownloader(){ return downloader; }
	public Settings getLang(){ return lang; }
	public GlobalSettings getSettings(){ return settings; }
	public VersionManager getVersionManager(){ return vm; }
	public Updater getUpdater(){ return updater; }
	
	public OptionSet getArguments(){ return args; }
	
	public TLauncherFrame getFrame(){ return frame; }
	public TLauncherNoGraphics getLoader(){ return loader; }
	
	public static UUID getClientToken(){ return clientToken; }
	
	public ProfileLoader getProfileLoader(){ return pl; }
	public ProfileManager getCurrentProfileManager(){ return pl.getSelected(); }
	
	public void reloadLocale() throws IOException {
		Locale locale = settings.getLocale(); U.log("Selected locale: " + locale);
		
		URL url = TLauncher.class.getResource("/lang/"+ locale +".ini");
		if(lang == null) lang = new Settings(url); else lang.reload(url);
		
		Alert.prepareLocal();
		Localizable.setLang(lang);
	}
	
	public void launch(MinecraftLauncherListener listener, boolean forceupdate) {
		MinecraftLauncher launcher = new MinecraftLauncher(this, listener, forceupdate, true);
		launcher.start();
	}
	
	public static void kill(){ U.log("Good bye!"); System.exit(0); }
	public void hide(){ U.log("Hiding..."); frame.setVisible(false); }
	public void show(){ U.log("Here I am!"); frame.setVisible(true); }
	
	public static double getVersion() { return VERSION; }
	public static String getBrand(){ return BRAND; }
	public static String[] getUpdateRepos() { return DEFAULT_UPDATE_REPO; }
	public static String getSettingsFile() { return SETTINGS; }
	
	/*___________________________________*/
	
	public static void main(String[] args){
		ExceptionHandler handler = ExceptionHandler.getInstance();
		Thread.setDefaultUncaughtExceptionHandler(handler);
		
		U.setPrefix("[TLauncher]");
		
		try { launch(args); }
		catch(Throwable e){
			e.printStackTrace();
			
			Alert.showError(e, true);
		}
	}
	
	private static void launch(String[] args) throws Exception {		
		U.log("Hello!");
		U.log("---");
		
	    U.log("Starting version", VERSION);

	    OptionSet set = ArgumentParser.parseArgs(args);
	    if(set == null) { new TLauncher(TLauncherState.FULL, args, null); return; }
	    if(set.has("help")) ArgumentParser.getParser().printHelpOn(System.out);

	    TLauncherState state = TLauncherState.FULL;
	    if (set.has("nogui")) state = TLauncherState.MINIMAL;

	    new TLauncher(state, args, set);
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
