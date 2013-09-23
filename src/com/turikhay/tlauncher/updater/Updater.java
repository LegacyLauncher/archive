package com.turikhay.tlauncher.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jsmooth.Wrapper;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class Updater {
	public final String link = "http://u.to/tlauncher-update/ixhQBA";
	public final Package type;
	
	private final Downloader d;
	private final URL url;
	
	private List<UpdaterListener> listeners = new ArrayList<UpdaterListener>();
	private Downloadable update_download;
	private Settings update_settings;
	
	private double found_version;
	private String found_link;
	
	private Downloadable launcher_download;
	private File launcher_destination;
	
	private File replace;
	
	public Updater(TLauncher t){
		this.d = t.getDownloader();
		this.type = (Wrapper.isAvailable())? Package.EXE : Package.JAR;
		this.replace = (Wrapper.isAvailable())? Wrapper.getExecutable() : FileUtil.getRunningJar();
		
		try {
			url = new URL(link);
		} catch (MalformedURLException e) {
			throw new TLauncherException("Cannot create update link!", e);
		}
		
		log("Updater enabled. Package type: "+type);
		if(type == Package.EXE) log("Working directory always MUST BE the same as launcher directory.");
	}
	
	public Updater(TLauncher t, Package type){
		this.d = t.getDownloader();
		this.type = type;
		
		try {
			url = new URL(link);
		} catch (MalformedURLException e) {
			throw new TLauncherException("Cannot create update link!", e);
		}
	}
	
	public void addListener(UpdaterListener l){ listeners.add(l); }
	public void removeListener(UpdaterListener l){ listeners.remove(l); }
	
	public void asyncFindUpdate(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				findUpdate();
			}
		});
	}
	
	public void findUpdate(){
		try{ findUpdate_(); }catch(Exception e){
			log("Error on searching for update", e);
			onUpdateNotifierError(e);
		}
	}
	
	private void findUpdate_() throws IOException {
		log("Searching for an update...");
		
		if(type == Package.EXE){
			File oldfile = new File(Wrapper.getExecutable().getAbsolutePath() + ".replace");
			if(oldfile.delete()) log("Old version has been deleted (.replace)");
		}
		onUpdaterRequests();
		
		update_download = new Downloadable(url);		
		HttpURLConnection connection = update_download.makeConnection();
		int code = connection.getResponseCode();
		switch(code){
		case 200:
			break;
		default:
			throw new IllegalStateException("Response code ("+code+") is not supported by Updater!");
		}
		
		InputStream is = connection.getInputStream();
		update_settings = new Settings(is); connection.disconnect();
		found_version = update_settings.getDouble("last-version");
		
		if(found_version <= 0.0) throw new IllegalStateException("Settings file is invalid!");
		if(TLauncher.VERSION >= found_version){
			if(TLauncher.VERSION > found_version){ log("Running version is newer than found ("+found_version+")"); }
			noUpdateFound(); return;
		}
		
		String current_link = update_settings.get(type.toLowerCase());
		found_link = current_link;
		
		onUpdateFound();
	}
	
	public void downloadUpdate() {
		try{ downloadUpdate_(); }catch(Exception e){ onUpdateError(e); }
	}
	
	private void downloadUpdate_() throws MalformedURLException {
		log("Downloading update...");
		onUpdaterDownloads();
		
		launcher_destination = new File(MinecraftUtil.getWorkingDirectory(), "tlauncher.updated"); launcher_destination.deleteOnExit();
		launcher_download = new Downloadable(found_link, launcher_destination);
		launcher_download.setHandler(new DownloadableHandler(){
			public void onStart() {}
			public void onCompleteError(){ onUpdateError(launcher_download.getError()); }
			public void onComplete(){ onUpdateDownloaded(); }
		});
		d.add(launcher_download);
		d.launch();
	}
	
	public String getLink(){
		return found_link;
	}
	
	public void saveUpdate(){
		try{ saveUpdate_(); }catch(Exception e){ onProcessError(e); }
	}
	
	private void saveUpdate_() throws IOException {
		log("Saving update... Launcher will be closed.");
		
		if(type == Package.EXE){
			File oldfile = new File(replace.toString()), newfile = new File(replace.toString() + ".replace");
			oldfile.renameTo(newfile);
		}
		
		FileInputStream in = new FileInputStream(launcher_destination);
		FileOutputStream out = new FileOutputStream(replace);
		
		byte[] buffer = new byte[65536];
		
		int curread = in.read(buffer);
		while(curread > 0){
			out.write(buffer, 0, curread);
			
			curread = in.read(buffer);
		}
		
		in.close(); out.close();
		System.exit(0);
	}
	
	public double getFoundVersion(){ return this.found_version; }
	public URI getFoundLinkAsURI(){ try {
		return new URI(found_link);
	} catch (URISyntaxException e) {
		e.printStackTrace();
		return null;
	} }
	
	private void onUpdaterRequests(){
		for(UpdaterListener l : listeners)
			l.onUpdaterRequesting(this);
	}
	
	private void onUpdateNotifierError(Throwable e){
		for(UpdaterListener l : listeners)
			l.onUpdaterRequestError(this, e);
	}
	
	private void noUpdateFound(){
		for(UpdaterListener l : listeners)
			l.onUpdaterNotFoundUpdate(this);
	}
	
	private void onUpdateFound(){
		for(UpdaterListener l : listeners)
			l.onUpdaterFoundUpdate(this);
	}
	
	private void onUpdaterDownloads(){
		for(UpdaterListener l : listeners)
			l.onUpdaterDownloading(this);
	}
	
	
	private void onUpdateDownloaded(){
		for(UpdaterListener l : listeners)
			l.onUpdaterDownloadSuccess(this);
	}
	
	private void onUpdateError(Throwable e){
		for(UpdaterListener l : listeners)
			l.onUpdaterDownloadError(this, e);
	}
	
	private void onProcessError(Throwable e){
		for(UpdaterListener l : listeners)
			l.onUpdaterProcessError(this, e);
	}
	
	private void log(Object... obj){ for(Object cobj : obj) U.log("[Updater]", cobj); }
	
	public enum Package {
		EXE, JAR;
		public String toLowerCase(){ return this.name().toLowerCase(); }
	}
}
