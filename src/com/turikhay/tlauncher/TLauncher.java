package com.turikhay.tlauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Locale;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import javax.swing.JFrame;

import net.minecraft.launcher_.updater.VersionManager;
import LZMA.LzmaInputStream;

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.Localizable;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class TLauncher {
	public final static double VERSION = 0.169;
	public final static Locale DEFAULT_LOCALE = Locale.US;
	public final static String[] SUPPORTED_LOCALE = new String[]{"ru_RU", "en_US", "uk_UA"};
	
	private Locale locale;
	private static TLauncher instance;
	
	private String[] args;
	
	private Settings lang;
	private GlobalSettings settings;
	private Downloader downloader;
	private Updater updater;
	private TLauncherFrame frame;
	
	private VersionManager vm;
	
	public TLauncher(String[] args) throws Exception {
		TLauncher.instance = this;
		
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
		U.setWorkingTo(this);
		
		this.args = args;
		
		long start = System.currentTimeMillis();
		
		settings = new GlobalSettings(); reloadLocale();
		downloader = new Downloader(10);
		updater = new Updater(this);
		vm = new VersionManager();
		
		frame = new TLauncherFrame(this);
		downloader.launch();
		
		init();
		
		long end = System.currentTimeMillis(), diff = end - start;
		U.log("Started! ("+diff+" ms.)");
	}
	
	private void init() throws IOException {
		final LoginForm lf = frame.getLoginForm();
		
		vm.addRefreshedListener(lf.versionchoice);
		if(lf.autologin.isEnabled()){
			vm.refreshVersions(true);
			lf.autologin.startLogin();
		} else {
			vm.asyncRefresh();
			vm.asyncRefreshResources();
		}
		
		updater.addListener(frame);
		updater.asyncFindUpdate();
	}
	
	public Downloader getDownloader(){ return downloader; }
	public TLauncherFrame getFrame(){ return frame; }
	public Settings getLang(){ return lang; }
	public GlobalSettings getSettings(){ return settings; }
	public Locale getLocale(){ return locale; }
	public VersionManager getVersionManager(){ return vm; }
	
	public void reloadLocale() throws IOException {
		locale = settings.getLocale(); U.log("Selected locale: "+locale);
		
		URL url = TLauncher.class.getResource("/lang/"+ locale +".ini");
		if(lang == null) lang = new Settings(url); else lang.reload(url);
		
		Alert.prepareLocal();
		Localizable.setLang(lang);
	}
	
	public void launch(MinecraftLauncherListener listener, boolean forceupdate) {
		MinecraftLauncher launcher = new MinecraftLauncher(this, listener, args, forceupdate);
		launcher.start();
	}
	
	public void runDefaultLauncher(){		
		Class<?>[] classes = new Class<?>[]{ JFrame.class, File.class, Proxy.class, PasswordAuthentication.class, String[].class, Integer.class };
		Object[] objects = new Object[]{ frame, MinecraftUtil.getWorkingDirectory(), Proxy.NO_PROXY, null, new String[0], Integer.valueOf(5)};
		
		MinecraftUtil.startLauncher(MinecraftUtil.getFile("launcher.jar"), classes, objects);
	}
	
	public void createDefaultLauncher(final boolean run) {
		final DownloadableContainer c = new DownloadableContainer();
		final Downloadable d = MinecraftUtil.getDownloadable("https://s3.amazonaws.com/Minecraft.Download/launcher/launcher.pack.lzma", false);
		
		c.setHandler(new DownloadableHandler(){
			public void onComplete(){ try {				
				LzmaInputStream in = new LzmaInputStream(new FileInputStream( d.getDestination() ));
				FileOutputStream out = new FileOutputStream(MinecraftUtil.getFile("launcher.pack"));
				
				byte[] buffer = new byte[65536];

				int read = in.read(buffer);
				while (read >= 1) {
					out.write(buffer, 0, read);
					read = in.read(buffer);
				}
				
				in.close(); out.close();
				
				JarOutputStream jarOutputStream = null;
				jarOutputStream = new JarOutputStream(new FileOutputStream(MinecraftUtil.getFile("launcher.jar")));
				Pack200.newUnpacker().unpack(MinecraftUtil.getFile("launcher.pack"), jarOutputStream);
				
				jarOutputStream.close();
				
				if(run) runDefaultLauncher();
			}catch(Exception e){ e.printStackTrace(); } }
			
			public void onCompleteError(){
				frame.getLoginForm().cancelLogin();
			}
			public void onStart() {}
		});
		
		c.add(d);
		downloader.add(c);
		downloader.launch();
	}
	public void kill(){ System.exit(0); }
	public void hide(){ U.log("Hiding..."); frame.setVisible(false); }
	public void show(){ U.log("Here I am!"); frame.setVisible(true); }
	
	/*___________________________________*/
	
	public static void main(String[] args){
		ExceptionHandler handler = new ExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
		
		try { launch(args); }
		catch(Throwable e){
			e.printStackTrace();
			
			Alert.showError(e, true);
		}
	}
	private static void launch(String[] args) throws Exception {
		
		U.log("Hello!");
		
		if(args.length > 0)
			U.log("All arguments will be passed in Minecraft directly");
		
		U.log("Starting version "+ VERSION +"...");
		
		new TLauncher(args);
	}
	
	public static TLauncher getInstance(){
		if(instance != null) return instance;
		throw new TLauncherException("Instance is not defined!");
	}
	public static Locale getSupported(){
		Locale using = Locale.getDefault();
		String using_name = using.toString();
		
		for(String supported : SUPPORTED_LOCALE)
			if(supported.equals(using_name))
				return using;
		
		return Locale.US;
	}
}
