package com.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.minecraft.events.ProfileListener;
import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.UpdateListener;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;

public class TLauncherFrame extends JFrame implements ProfileListener, DownloadListener, UpdaterListener, UpdateListener {
	public static final Color backgroundColor = new Color(141, 189, 233);
	private final TLauncherFrame instance = this;
	final TLauncher t;
	private static final long serialVersionUID = 5949683935156305416L;
	
	int width, height;
	
	Image bgimage;
	public Image favicon;
	Image sun;
	
	GlobalSettings global;
	Settings lang;
	Downloader d;
	
	MainPane mp;
	ProgressBar pb;
	
	LoginForm lf;
	SettingsForm sf;
	
	ProfileCreatorForm spcf;
	
	private boolean pb_started;
	private ProfileManager pm;
	
	public TLauncherFrame(TLauncher tlauncher){
		this.t = tlauncher; this.global = t.getSettings(); this.lang = t.getLang();
		this.d = t.getDownloader(); this.pm = t.getCurrentProfileManager();
		
		try{ this.loadResources(); }catch(Exception e){ throw new TLauncherException("Cannot load required resource!", e); }
		
		int[] w_sizes = global.getWindowSize();
		
		width = w_sizes[0];
		height = w_sizes[1];
		
		long start = System.currentTimeMillis();
		U.log("Preparing main frame...");
		
		this.prepareFrame();
		
		long end = System.currentTimeMillis(), diff = end - start;
		U.log("Prepared:", diff);
		
		this.setVisible(true);
		this.requestFocusInWindow();
		
		if(global.isFirstRun())
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
	
	private void initFontSize() {
		try{
			UIDefaults defaults = UIManager.getDefaults();
		
			for (Enumeration<?> e = defaults.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				Object value = defaults.get(key);
				if (value instanceof Font) {
					Font font = (Font) value;
					int newSize = Math.round(font.getSize() + 1);
					if (value instanceof FontUIResource) {
						defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
					} else {
						defaults.put(key, new Font(font.getName(), font.getStyle(), newSize));
					}
				}
			}
		} catch(Exception e){ U.log("Cannot change font sizes!", e); }
	}
	
	private void initUILocale(){		
		UIManager.put("OptionPane.yesButtonText", lang.nget("ui.yes"));
		UIManager.put("OptionPane.noButtonText", lang.nget("ui.no"));
		UIManager.put("OptionPane.cancelButtonText", lang.nget("ui.cancel"));
	}
	
	public void updateLocales(){		
		try{ t.reloadLocale(); }catch(Exception e){ U.log("Cannot reload settings!", e); return; }
		
		Console.updateLocale();
		setWindowTitle(); initUILocale();
		updateContainer(this, true);
	}
	
	public static void updateContainer(Container container, boolean deep){
		for(Component c : container.getComponents()){
			if(c instanceof LocalizableComponent) ((LocalizableComponent) c).updateLocale();
			if(c instanceof Container && deep) updateContainer((Container) c, true);
		}
	}
	
	private void prepareFrame(){
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			U.log("Can't set system look and feel.");
			e.printStackTrace();
		}
		
		this.initUILocale();
		this.setBackground(backgroundColor);
		this.initFontSize();
		this.setWindowTitle();
		this.resizeWindow(width, height);
		this.setIconImage(favicon);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				TLauncher.kill();
			}
	    });
		
		this.addComponentListener(new ComponentListener(){
			public void componentResized(ComponentEvent e) {
				mp.onResize();
			}
			public void componentShown(ComponentEvent e) {
				mp.startBackground();
				
				instance.validate();
				instance.repaint();
				instance.toFront();
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {
				mp.suspendBackground();
			}
		});
		
		long start = System.currentTimeMillis();
		U.log("Preparing components...");
		
		sf = new SettingsForm(this);
		lf = new LoginForm(this);
		
		spcf = new ProfileCreatorForm(this);
		
		pb = new ProgressBar(this);
		
		start = System.currentTimeMillis();
		U.log("Preparing main pane...");
		
		mp = new MainPane(this);
		
		long end = System.currentTimeMillis(), diff = end - start;
		U.log("Prepared pane:", diff);
		
		add(mp);
		add("South", pb);
		
		end = System.currentTimeMillis(); diff = end - start;
		U.log("Components prepared:", diff);
		
		start = System.currentTimeMillis();
		U.log("Packing main frame...");
		
		pack();
		
		end = System.currentTimeMillis(); diff = end - start;
		U.log("Packed:", diff);
		
		start = System.currentTimeMillis();
		U.log("Resizing main pane...");
		
		mp.onResize();
		
		end = System.currentTimeMillis(); diff = end - start;
		U.log("Main pane resized:", diff);
	}
	
	private void loadResources() throws IOException {
		bgimage = ImageIO.read(TLauncherFrame.class.getResource("grass.png"));
		favicon = ImageIO.read(TLauncherFrame.class.getResource("favicon.png"));
		sun = ImageIO.read(TLauncherFrame.class.getResource("sun.png"));
	}
	
	private void setWindowTitle() {
		String translator = lang.nget("translator"); 
		this.setTitle("TLauncher " + TLauncher.getVersion() + " (by turikhay"+ ((translator != null)?", translated by "+translator:"") +")");
	}
	
	public LoginForm getLoginForm(){
		return lf;
	}
	
	public ProgressBar getProgressBar(){
		return pb;
	}
	
	public ProfileManager getProfileManager(){
		return pm;
	}
	
	public void onDownloaderStart(Downloader d, int files) { if(pb_started) return;
		pb_started = true;
		
		pb.progressStart(); pb.setIndeterminate(true);
		pb.setCenterString(lang.get("progressBar.init"));
		pb.setEastString(lang.get("progressBar.downloading" + ((files == 1)?"-one":""), "i", files));
	}

	public void onDownloaderAbort(Downloader d) {
		pb_started = false;
		pb.progressStop();
	}
	public void onDownloaderComplete(Downloader d) {
		pb_started = false;
		pb.progressStop();
	}
	public void onDownloaderFileComplete(Downloader d, Downloadable f) {		
		pb.setIndeterminate(false);
		pb.setWestString(lang.get("progressBar.completed", "f", f.getFilename() ));
		pb.setEastString(lang.get("progressBar.remaining", "i", d.getRemaining() ));
	}
	public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {
		int i = d.getRemaining();
		if(i == 0){
			onDownloaderComplete(d);
			return;
		}
		String path = "download.error" + ((error == null)?".unknown":"");
		
		pb.setIndeterminate(false);
		pb.setCenterString(lang.get(path, "f", file.getFilename(), "e", error.toString()));
	}
	public void onDownloaderProgress(Downloader d, int progress, double speed) {		
		if(progress > 0){
			if(pb.getValue() > progress) return; // Something from a "lazy" thread, ignore.
			
			pb.setIndeterminate(false);
			pb.setValue(progress);
			pb.setCenterString(progress + "%");
			
			//if(speed > 1.0)
				//pb.setEastString("~ " + U.setFractional(speed, 1) + " " + lang.get("progressBar.speed"));			
		}
	}
	//
	public void onUpdaterRequesting(Updater u) {}
	public void onUpdaterRequestError(Updater u) {}
	public void onUpdaterNotFoundUpdate(Updater u) {}
	//
	public void onUpdateFound(Updater u, Update upd) {
		double version = upd.getVersion();
		
		Alert.showWarning(lang.get("updater.found.title"), lang.get("updater.found", "v", version), upd.getDescription());
		
		if(Updater.isAutomode()){
			upd.addListener(this);
			upd.download();
			
			return;
		}
		
		if(openUpdateLink(upd.getDownloadLink()))
			TLauncher.kill();
	}
	public void onUpdateError(Update u, Throwable e) {
		if(Alert.showQuestion(lang.get("updater.error.title"), lang.get("updater.download-error"), e, true))
			openUpdateLink(u.getDownloadLink());
	}
	public void onUpdateDownloading(Update u) {}
	public void onUpdateDownloadError(Update u, Throwable e) {
		this.onUpdateError(u, e);
	}
	public void onUpdateReady(Update u) {
		Alert.showWarning(lang.get("updater.downloaded.title"), lang.get("updater.downloaded"));
		u.apply();
	}
	public void onUpdateApplying(Update u) {}
	public void onUpdateApplyError(Update u, Throwable e) {
		if(Alert.showQuestion(lang.get("updater.save-error.title"), lang.get("updater.save-error"), e, true))
			openUpdateLink(u.getDownloadLink());
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

	public void onProfilesRefreshed(ProfileManager pm) {}
	public void onProfileManagerChanged(ProfileManager pm) {
		this.pm = pm;
	}
}
