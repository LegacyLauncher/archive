package com.turikhay.tlauncher.updater;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public class Updater {
	public static final String[] links = TLauncher.getUpdateRepos();
	public static final URI[] URIs = makeURIs();
	
	private final Downloader d;
	
	private List<UpdaterListener> listeners = Collections.synchronizedList(new ArrayList<UpdaterListener>());
	public void addListener(UpdaterListener l){ listeners.add(l); }
	public void removeListener(UpdaterListener l){ listeners.remove(l); }
	
	private Update found;
	
	public Updater(Downloader d){
		this.d = d;
		
		if(!PackageType.isCurrent(PackageType.JAR)){
			File oldfile = Updater.getTempFile();
			if(oldfile.delete()) log("Old version has been deleted (.update)");
		}
		
		log("Initialized.");
		log("Package type:", PackageType.getCurrent());
	}
	public Updater(TLauncher t){ this(t.getDownloader()); }
	
	public void findUpdate(){		
		log("Requesting an update...");
		this.onUpdaterRequests();
		
		int attempt = 0;
		for(URI uri : URIs){ ++attempt;
			log("Attempt #"+attempt+". URL:", uri);
			try{
				Downloadable downloadable = new Downloadable(uri.toURL());
				HttpURLConnection connection = downloadable.makeConnection();
				
				int code = connection.getResponseCode();
				switch(code){
				case 200:
					break;
				default:
					throw new IllegalStateException("Response code ("+code+") is not supported by Updater!");
				}
				
				InputStream is = connection.getInputStream();
				Settings parsed = new Settings(is);
				connection.disconnect();
				
				Update update = new Update(d, parsed);
				double version = update.getVersion();
				
				log("Success!");
				
				TLauncher.getInstance();
				if(TLauncher.getVersion() > version) {
					TLauncher.getInstance();
					log("Found version is older than running:", version, "("+TLauncher.getVersion()+")");
				}
				
				if(update.getDownloadLink() == null){
					log("An update for current package type is not available.");
					return;
				}
				
				if(TLauncher.getVersion() >= version){
					Ad ad = new Ad(parsed);
					if(ad.canBeShown()) onAdFound(ad);
					
					noUpdateFound(); return;
				}
				
				log("Found actual version:", version);
				this.found = update;
				onUpdateFound(update);
				return;
			}catch(Exception e){
				log("Cannot get update information", e);
			}
		}
		
		log("Updating is impossible - cannot get any information.");
		this.onUpdaterRequestError();
	}
	
	public Update getUpdate(){
		return found;
	}
	
	public void asyncFindUpdate(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				findUpdate();
			}
		});
	}
	
	private void onUpdaterRequests(){
		synchronized(listeners){
			for(UpdaterListener l : listeners)
				l.onUpdaterRequesting(this);
		}
	}
	
	private void onUpdaterRequestError(){
		synchronized(listeners){
			for(UpdaterListener l : listeners)
				l.onUpdaterRequestError(this);
		}
	}
	
	private void onUpdateFound(Update u){
		synchronized(listeners){
			for(UpdaterListener l : listeners)
				l.onUpdateFound(this, u);
		}
	}
	
	private void noUpdateFound(){
		synchronized(listeners){
			for(UpdaterListener l : listeners)
				l.onUpdaterNotFoundUpdate(this);
		}
	}
	
	private void onAdFound(Ad ad){
		synchronized(listeners){
			for(UpdaterListener l : listeners)
				l.onAdFound(this, ad);
		}
	}
	
	public static boolean isAutomodeFor(PackageType pt){
		if(pt == null)
			throw new NullPointerException("PackageType is NULL!");
		
		switch(pt){
		case EXE: case JAR: return true;
		default:
			throw new IllegalArgumentException("Unknown PackageType!");
		}
	}
	
	public static boolean isAutomode(){
		return isAutomodeFor(PackageType.getCurrent());
	}
	
	public static File getFileFor(PackageType pt){
		if(pt == null)
			throw new NullPointerException("PackageType is NULL!");
		
		switch(pt){
		case EXE: case JAR: return FileUtil.getRunningJar();
		default:
			throw new IllegalArgumentException("Unknown PackageType!");
		}
	}
	
	public static File getFile(){
		return getFileFor(PackageType.getCurrent());
	}
	
	public static File getUpdateFileFor(PackageType pt){
		return new File(getFileFor(pt).getAbsolutePath() + ".update");
	}
	
	public static File getUpdateFile(){
		return getUpdateFileFor(PackageType.getCurrent());
	}
	
	public static File getTempFileFor(PackageType pt){
		return new File(getFileFor(pt).getAbsolutePath() + ".replace");
	}
	
	public static File getTempFile(){
		return getTempFileFor(PackageType.getCurrent());
	}
	
	private static URI[] makeURIs(){
		int len = links.length;
		URI[] r = new URI[len];
		
		for(int i=0;i<len;i++)
			try{ r[i] = new URL(links[i]).toURI(); }
			catch(Exception e){ throw new TLauncherException("Cannot create link from at i:"+i, e); }
		
		return r;
	}
	private static void log(Object... obj){ U.log("[Updater]", obj); }
}
