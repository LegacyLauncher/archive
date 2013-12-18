package com.turikhay.tlauncher.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.turikhay.tlauncher.Bootstrapper;
import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.handlers.DownloadableHandler;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.util.U;

public class Update {
	private int step;
	
	private double version;
	private String description;
	
	private Map<PackageType, URI> links = new HashMap<PackageType, URI>();
	
	private final Downloader d;
	private boolean isDownloading;
	
	private List<UpdateListener> listeners = Collections.synchronizedList(new ArrayList<UpdateListener>());
	public void addListener(UpdateListener l){ listeners.add(l); }
	public void removeListener(UpdateListener l){ listeners.remove(l); }
	
	Update(Downloader d, double version, String description){
		if(d == null)
			throw new NullPointerException("Downloader is NULL!");
		
		this.d = d;
		setVersion(version);
		setDescription(description);
	}
	
	Update(Downloader d, Settings settings){
		if(d == null)
			throw new NullPointerException("Downloader is NULL!");
		if(settings == null)
			throw new NullPointerException("Settings is NULL!");
		
		this.d = d;
		setVersion(settings.getDouble("latest"));
		setDescription(settings.nget("description"));
		
		for(String key : settings.getKeys())
			try{ links.put(PackageType.valueOf(key.toUpperCase()), U.makeURI(settings.nget(key))); }catch(Exception e){}
		
		U.log(links);
	}
	
	public URI getDownloadLinkFor(PackageType pt){
		return links.get(pt);
	}
	
	public URI getDownloadLink(){
		return getDownloadLinkFor(PackageType.getCurrent());
	}
	
	public double getVersion(){
		return version;
	}
	
	public String getDescription(){
		return description;
	}
	
	public int getStep(){
		return step;
	}
	
	public void download(){
		downloadFor(PackageType.getCurrent());
	}
	
	public void downloadFor(PackageType pt){
		try{ downloadFor_(pt); }catch(Exception e){ onUpdateError(e); }
	}
	
	private void downloadFor_(PackageType pt) throws Exception {
		if(step > Step.NONE.ordinal())
			throw new IllegalStepException(step);
		
		log(0);
		
		URI download_link = getDownloadLinkFor(pt);
		if(download_link == null)
			throw new NullPointerException("Update for package \""+pt+"\" is not found");
		log(1);
		
		File destination = Updater.getUpdateFileFor(pt);
		destination.deleteOnExit();
			
		final Downloadable downloadable = new Downloadable(download_link.toURL(), destination);
		downloadable.addHandler(new DownloadableHandler(){
			public void onStart(){}
			public void onCompleteError(){
				isDownloading = false;
				step = Step.NONE.ordinal(); onUpdateDownloadError(downloadable.getError());
			}
			public void onComplete(){
				isDownloading = false;
				step = Step.DOWNLOADED.ordinal(); onUpdateReady();
			}
			public void onAbort(){
				isDownloading = false;
				step = Step.NONE.ordinal();
			}
		});
		
		log(2);
		onUpdateDownloading();
		
		isDownloading = true;
		d.add(downloadable);
		d.startLaunch();
		
		while(isDownloading) U.sleepFor(1000);
		log(3);
	}
	
	public void apply(){
		applyFor(PackageType.getCurrent());
	}
	
	public void applyFor(PackageType pt){
		try{ applyFor_(pt); }catch(Exception e){ onUpdateApplyError(e); }
	}
	
	private void applyFor_(PackageType pt) throws Exception {
		if(step < Step.DOWNLOADED.ordinal())
			throw new IllegalStepException(step);
		
		log("Saving update... Launcher will be reopened.");
		
		File replace = Updater.getFileFor(pt), replacer = Updater.getUpdateFileFor(pt);
		replacer.deleteOnExit();
		
		String[] args = (TLauncher.getInstance() != null)? TLauncher.getInstance().sargs : new String[0];
		ProcessBuilder builder = Bootstrapper.buildProcess(args);
		
		FileInputStream in = new FileInputStream(replacer);
		FileOutputStream out = new FileOutputStream(replace);
		
		onUpdateApplying();
		
		byte[] buffer = new byte[65536];
		
		int curread = in.read(buffer);
		while(curread > 0){
			out.write(buffer, 0, curread);
			
			curread = in.read(buffer);
		}
		
		in.close(); out.close();
		builder.start();
		System.exit(0);
	}
	
	void setVersion(double v){
		if(v <= 0.0)
			throw new IllegalArgumentException("Invalid version!");
		this.version = v;
	}
	
	void setDescription(String desc){
		this.description = desc;
	}
	
	void setLinkFor(PackageType pt, URI link){
		if(pt == null)
			throw new NullPointerException("PackageType is NULL!");
		if(link == null)
			throw new NullPointerException("URI is NULL!");
		
		if(links.containsKey(pt)) links.remove(pt);
		links.put(pt, link);
	}
	
	private void onUpdateError(Throwable e){
		synchronized(listeners){
			for(UpdateListener l : listeners)
				l.onUpdateError(this, e);
		}
	}	
	private void onUpdateDownloading(){
		synchronized(listeners){
			for(UpdateListener l : listeners)
				l.onUpdateDownloading(this);
		}
	}
	private void onUpdateDownloadError(Throwable e){
		synchronized(listeners){
			for(UpdateListener l : listeners)
				l.onUpdateDownloadError(this, e);
		}
	}
	private void onUpdateReady(){
		synchronized(listeners){
			for(UpdateListener l : listeners)
				l.onUpdateReady(this);
		}
	}
	private void onUpdateApplying(){
		synchronized(listeners){
			for(UpdateListener l : listeners)
				l.onUpdateApplying(this);
		}
	}
	private void onUpdateApplyError(Throwable e){
		synchronized(listeners){
			for(UpdateListener l : listeners)
				l.onUpdateApplyError(this, e);
		}
	}
		
	public enum Step {
		NONE, DOWNLOADING, DOWNLOADED, UPDATING;
	}
	
	private static void log(Object... obj){ U.log("[Updater]", obj); }
	private static String getMessageForStep(int step, String description){
		String r = "Illegal action on step #" + step;
		
		for(Step curstep : Step.values())
			if(curstep.ordinal() == step){ r = curstep.toString(); break; }
		
		if(description != null) r += " : " + description;
		
		return r;
	}
	public class IllegalStepException extends RuntimeException {
		private static final long serialVersionUID = -1988019882288031411L;
		
		IllegalStepException(int step, String description){
			super(getMessageForStep(step, description));
		}
		IllegalStepException(int step){
			super(getMessageForStep(step, null));
		}
		
	}
}
