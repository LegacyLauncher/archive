package com.turikhay.tlauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.PasswordAuthentication;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import javax.swing.JFrame;

import net.minecraft.launcher_.updater.LocalVersionList;
import net.minecraft.launcher_.updater.RemoteVersionList;
import net.minecraft.launcher_.updater.VersionManager;

import LZMA.LzmaInputStream;

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.downloader.DownloadableHandler;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.ExceptionHandler;
import com.turikhay.tlauncher.minecraft.MinecraftLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.timer.Timer;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.LoginForm;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class TLauncher extends Thread {
	public final static double VERSION = 0.1;
	
	private boolean isAvaiable = true;
	
	private String[] args;
	
	public final Settings settings;
	public final Downloader downloader;
	public final TLauncherFrame frame;
	public final Timer timer;
	
	public final VersionManager vm;
	
	public TLauncher(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler.getInstance());
		U.setWorkingTo(this);
		
		this.args = args;
		
		settings = new GlobalSettings();
		downloader = new Downloader();
		timer = new Timer();
		vm = new VersionManager(new LocalVersionList(MinecraftUtil.getWorkingDirectory()), new RemoteVersionList());
		
		frame = new TLauncherFrame(this);
		downloader.launch();
		
		this.init();
	}
	
	private void init() throws IOException {
		LoginForm lf = frame.getLoginForm();
		
		vm.addRefreshedVersionsListener(lf);
		vm.refreshVersions();
	}
	
	public void launch(MinecraftLauncherListener listener, String username, String version, boolean forceupdate) {
		MinecraftLauncher launcher = new MinecraftLauncher(this, listener, version, forceupdate, username, args);
		launcher.run();
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
	
	public boolean isAvailable(){ return isAvaiable; }
	public void kill(){ this.isAvaiable = false; }
	public void hide(){ U.log("Hiding..."); frame.setVisible(false); }
	public void show(){ U.log("Here I am!"); frame.setVisible(true); }
	
	/*___________________________________*/
	
	public static void main(String[] args){
		ExceptionHandler handler = new ExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
		
		try { launch(args); System.exit(0); }
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
		TLauncher l = new TLauncher(args);
		l.start();
		U.log("Started!");
		
		while(l.isAvailable()){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				throw new TLauncherException("Runner cannot sleep.", e);
			}
		}
		
		U.linelog("Good bye!");
	}
}
