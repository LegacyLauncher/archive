package com.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.tlauncher.util.U;

public class TLauncherFrame extends JFrame implements DownloadListener, UpdaterListener {
	private final TLauncherFrame instance = this;
	final TLauncher t;	
	private static final long serialVersionUID = 5949683935156305416L;
	
	int width, height;
	Color bgcolor = new Color(141, 189, 233);
	
	Image bgimage, favicon, sun;
	
	Settings global, lang;
	Downloader d;
	
	MainContainer mc;
	ProgressBar pb;
	
	LoginForm lf;
	SettingsForm sf;
	
	private boolean pb_started;
	
	public TLauncherFrame(TLauncher tlauncher){		
		this.t = tlauncher; this.global = t.getSettings(); this.lang = t.getLang();
		this.d = t.getDownloader();
		
		try{ this.loadResources(); }catch(Exception e){ throw new TLauncherException("Cannot load required resource!", e); }
		
		width = global.getInteger("minecraft.size.width");
		height = global.getInteger("minecraft.size.height");
		
		this.prepareFrame();
		
		this.setVisible(true);
		this.requestFocusInWindow();
		
		if(GlobalSettings.firstRun)
			Alert.showAsyncWarning(lang.get("firstrun.title"), U.w(lang.get("firstrun"), 90));
		
		this.d.addListener(this);
	}
	
	public void resizeWindow(int w, int h){
		Dimension sizes = new Dimension(width = w, height = h);
		
		this.setPreferredSize(sizes);
		this.setMinimumSize(sizes);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
	}
	
	public void updateLocales(){
		try{ t.reloadLocale(); }catch(Exception e){ U.log("Cannot reload settings!", e); return; }
		setWindowTitle();
		updateContainer(this, true);
	}
	
	public static void updateContainer(Container container, boolean deep){
		for(Component c : container.getComponents()){
			if(c instanceof LocalizableComponent){ ((LocalizableComponent) c).updateLocale(); }
			if(c instanceof Container && deep) updateContainer((Container) c, true);
		}
	}
	
	private void prepareFrame(){
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			U.log("Can't set system look and feel.");
			e.printStackTrace();
		}
		
		this.setWindowTitle();	
		this.resizeWindow(width, height);
		this.setIconImage(favicon);
		
		this.addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				t.kill();
			}
	    });
		
		sf = new SettingsForm(this);
		lf = new LoginForm(this);
		
		pb = new ProgressBar(this);
		
		mc = new MainContainer(this);
		
		add(mc);
		add("South", pb);
		
		pack();
	}
	
	private void loadResources() throws IOException {
		bgimage = ImageIO.read(TLauncherFrame.class.getResource("grass.png"));
		favicon = ImageIO.read(TLauncherFrame.class.getResource("favicon.png"));
		sun = ImageIO.read(TLauncherFrame.class.getResource("sun.png"));
	}
	
	private void setWindowTitle() {
		String translator = lang.nget("translator");	
		this.setTitle("TLauncher " + TLauncher.VERSION + " (by turikhay"+ ((translator != null)?", translated by "+translator:"") +")");
	}
	
	public LoginForm getLoginForm(){
		return lf;
	}
	
	public ProgressBar getProgressBar(){
		return pb;
	}
	
	public void onDownloaderStart(Downloader d, int files) { if(pb_started) return;
		pb_started = true;
		
		pb.progressStart(); pb.setIndeterminate(true);
		pb.setCenterString(lang.get("progressBar.init"));
		pb.setEastString(lang.get("progressBar.downloading" + ((files == 1)?"-one":""), "i", files));
	}
	public void onDownloaderComplete(Downloader d) {
		pb_started = false;
		pb.progressStop();
	}
	public void onDownloaderFileComplete(Downloader d, Downloadable f) {
		int i = d.getRemaining();
		
		pb.setEastString(lang.get("progressBar.remaining" + ((i == 1)?"-one":""), "i", i));
		pb.setWestString(lang.get("progressBar.completed", "f", f.getFilename() ));
	}
	public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {
		String path = "download.error" + ((error == null)?".unknown":"");
		
		pb.setIndeterminate(false);
		pb.setCenterString(lang.get(path, "f", file.getFilename(), "e", error.toString()));
		pb.setWestString(null);
		pb.setEastString(null);
	}
	public void onDownloaderProgress(Downloader d, int progress) {
		
		if(progress > 0){
			pb.setIndeterminate(false);
			pb.setValue(progress);
			pb.setCenterString(progress + "%");
		} else {
			pb.setIndeterminate(true);
			pb.setCenterString(null);
		}
	}
	public void onUpdaterRequesting(Updater u) {}
	public void onUpdaterRequestError(Updater u, Throwable e) {
		U.log("Error occurred while getting update:", e);
	}
	public void onUpdaterFoundUpdate(final Updater u) {
		double found_version = u.getFoundVersion();
		
		if(global.getDouble("updater.disallow") == found_version){
			U.log("User cancelled updating to this version.");
			return;
		}
		
		boolean yes = Alert.showQuestion(lang.get("updater.found.title"), lang.get("updater.found", "v", found_version), true);
		
		if(yes){ u.downloadUpdate(); return; }
		
		U.log("You don't want to update? Oh, okay... I will not disturb you with this version anymore.");
		global.set("updater.disallow", found_version);
		
	}
	public void onUpdaterNotFoundUpdate(Updater u) {}
	public void onUpdaterDownloading(Updater u) {}
	public void onUpdaterDownloadSuccess(Updater u) {
		Alert.showWarning(lang.get("updater.downloaded.title"), lang.get("updater.downloaded"));
		
		global.set("gui.sun", true);
		u.saveUpdate();
	}

	public void onUpdaterDownloadError(Updater u, Throwable e) {
		Alert.showError(lang.get("updater.error.title"), lang.get("updater.error.title"), e);
	}

	public void onUpdaterProcessError(Updater u, Throwable e) {
		Alert.showError(lang.get("updater.save-error.title"), lang.get("updater.save-error"), e);
	}
}
