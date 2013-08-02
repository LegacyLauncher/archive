package com.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.timer.Timer;
import com.turikhay.tlauncher.util.U;

public class TLauncherFrame extends JFrame implements DownloadListener {
	private final TLauncherFrame instance = this;
	final TLauncher t;	
	private static final long serialVersionUID = 5949683935156305416L;
	
	int width, height;
	Color bgcolor = new Color(141, 189, 233);
	
	Image bgimage;
	Image favicon;
	Image settings;
	Image sun;
	
	URL langfile;
	
	Settings global, lang;
	Downloader d;
	
	Timer ti;
	
	MainContainer mc;
	ProgressBar pb;
	LoginForm lf;
	
	private boolean pb_started;
	
	public TLauncherFrame(TLauncher tlauncher){		
		super("TLauncher");
		
		this.t = tlauncher; this.global = t.settings;
		this.d = t.downloader; this.ti = t.timer;
		
		try{ this.loadResources(); }catch(Exception e){ throw new TLauncherException("Cannot load required resource!", e); }
		try { this.lang = new Settings(langfile); } catch (IOException e) { throw new TLauncherException("Cannot read language file!", e); }
		
		width = global.getInteger("minecraft.width");
		height = global.getInteger("minecraft.height");
		
		this.prepareFrame();
		
		this.setVisible(true);
		this.requestFocusInWindow();
		
		this.d.addListener(this);
		this.ti.start();
	}
	
	private void prepareFrame(){
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			U.log("Can't set system look and feel.");
			e.printStackTrace();
		}
		
		Dimension sizes = new Dimension(width, height);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(sizes);
		this.setMinimumSize(sizes);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		
		this.setIconImage(favicon);
		
		this.addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				t.kill();
			}
	    });
		
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
		settings = ImageIO.read(TLauncherFrame.class.getResource("settings.png"));
		sun = ImageIO.read(TLauncherFrame.class.getResource("sun.png"));
		
		langfile = TLauncherFrame.class.getResource("/lang.ini");
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
}
