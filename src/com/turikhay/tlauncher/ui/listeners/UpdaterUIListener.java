package com.turikhay.tlauncher.ui.listeners;

import java.net.URI;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;

public class UpdaterUIListener implements UpdaterListener, UpdateListener {
	private final TLauncher t;
	
	private final LangConfiguration lang;
	private final Configuration global;
	
	private Update hiddenUpdate;
	private Throwable hiddenError;
	
	public UpdaterUIListener(TLauncher tlauncher){
		this.t = tlauncher;
		
		this.lang = t.getLang();
		this.global = t.getSettings();
	}
	
	public void onUpdaterRequesting(Updater u) {}
	public void onUpdaterRequestError(Updater u) {}
	public void onUpdaterNotFoundUpdate(Updater u) {}
	//
	public void onUpdateFound(Update upd, boolean force, boolean async) {
		boolean download = true;
		
		if(!force && t.isLauncherWorking()){
			download = global.getConnectionQuality().equals(ConnectionQuality.GOOD);
			this.hiddenUpdate = upd;
		}
		
		boolean shown = force || hiddenUpdate == null;
		
		double version = upd.getVersion();
		
		if(shown)
			Alert.showWarning(lang.get("updater.found.title"), lang.get("updater.found", version), upd.getDescription());
		
		block();
		
		if(Updater.isAutomode()){
			upd.addListener(this);
			
			if(download)
				upd.download(async);
			
			return;
		}
		
		if(shown && openUpdateLink(upd.getDownloadLink()))
			TLauncher.kill();
	}
	public void onUpdateFound(Update upd) {
		onUpdateFound(upd, false, false);
	}
	public void onUpdateError(Update u, Throwable e) {
		if(hiddenUpdate != null)
			this.hiddenError = e;
		else
			if(Alert.showQuestion(lang.get("updater.error.title"), lang.get("updater.download-error"), e, true))
				openUpdateLink(u.getDownloadLink());
		
		unblock();
	}
	public void onUpdateDownloading(Update u) {}
	public void onUpdateDownloadError(Update u, Throwable e) {
		this.onUpdateError(u, e);
	}
	public void onUpdateReady(Update u) {
		this.onUpdateReady(u, false, false);
	}
	public void onUpdateReady(Update u, boolean force, boolean showChangeLog) {		
		if(!force && u.equals(hiddenUpdate)) return;
		
		Alert.showWarning(lang.get("updater.downloaded.title"), lang.get("updater.downloaded"), showChangeLog? u.getDescription() : null);
		u.apply();
	}
	public void onUpdateApplying(Update u) {}
	public void onUpdateApplyError(Update u, Throwable e) {
		if(Alert.showQuestion(lang.get("updater.save-error.title"), lang.get("updater.save-error"), e, true))
			openUpdateLink(u.getDownloadLink());
		
		unblock();
	}
	private boolean openUpdateLink(URI uri){
		try{ OperatingSystem.openLink(uri); }
		catch(Exception e){
			Alert.showError(lang.get("updater.found.cannotopen.title"), lang.get("updater.found.cannotopen"), uri);
			return false;
		}
		return true;
	}
	//
	public void onAdFound(Updater u, Ad ad) {
		if(global.getInteger("updater.ad") == ad.getID()) return;
		if(!ad.canBeShown()) return;
		
		global.set("updater.ad", ad.getID());
		ad.show(false);
	}
	
	public void applyDelayedUpdate(){
		if(hiddenUpdate == null) return;
		
		int step = hiddenUpdate.getStep();
		
		if(hiddenError != null){
			this.onUpdateError(hiddenUpdate, hiddenError);
			return;
		}
		
		switch(step){
		case Update.NONE:
			onUpdateFound(hiddenUpdate, true, true);
		case Update.DOWNLOADING:
			hiddenUpdate = null;
			return;
		case Update.DOWNLOADED:
			onUpdateReady(hiddenUpdate, true, true);
			return;
		}
	}
	
	private void block(){
		Blocker.block(t.getFrame().mp, "updater");
	}
	
	private void unblock(){
		Blocker.unblock(t.getFrame().mp, "updater");
	}
}
