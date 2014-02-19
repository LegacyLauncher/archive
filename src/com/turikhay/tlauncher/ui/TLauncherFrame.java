package com.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.console.Console;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import com.turikhay.util.U;

public class TLauncherFrame extends JFrame {
	private static final long serialVersionUID = 5077131443679431434L;
	
	private final static List<Image> favicons = new ArrayList<Image>();
	
	private final TLauncherFrame instance;
	
	public final TLauncher tlauncher;
	public final Configuration settings;
	public final LangConfiguration lang;
	//
	private final int[] windowSize;
	//
	public final MainPane mp;
	//
	
	public TLauncherFrame(TLauncher t){
		this.instance = this;
		
		this.tlauncher = t;
		this.settings = t.getSettings();
		this.lang = t.getLang();
		
		this.windowSize = settings.getWindowSize();
		
		TLauncherFrame.initLookAndFeel();
		TLauncherFrame.initFontSize();
		
		this.setUILocale();
		this.setWindowSize();
		this.setWindowTitle();
		this.setIconImages(getFavicons());
		
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
				instance.validate();
				instance.repaint();
				instance.toFront();
				
				mp.showBackground();
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {
				mp.hideBackground();
			}
		});
		
		log("Preparing main pane...");
		mp = new MainPane(this);
		
		add(mp);
		
		log("Packing main frame...");
		pack();
		
		log("Resizing main pane...");
		mp.onResize();
		mp.showBackground();
		
		this.setVisible(true);
		
		if(settings.isFirstRun())
			Alert.showAsyncWarning("firstrun");
	}
	
	public void updateLocales(){		
		try{ tlauncher.reloadLocale(); }catch(Exception e){ log("Cannot reload settings!", e); return; }
		
		Console.updateLocale();
		LocalizableMenuItem.updateLocales();
		
		setWindowTitle(); setUILocale();
		updateContainer(this, true);
	}
	
	private void setWindowSize(){
		Dimension sizes = new Dimension(windowSize[0], windowSize[1]);
		
		this.setPreferredSize(sizes);
		this.setMinimumSize(sizes);
		this.setLocationRelativeTo(null);
	}
	
	private void setWindowTitle() {
		String
			translator = lang.nget("translator"),
			copyright = "(by turikhay"+ ((translator != null)?", translated by "+translator : "") +")",
			brand = TLauncher.getBrand() + " " + TLauncher.getVersion();
		
		this.setTitle("TLauncher " + brand + " " + copyright);
	}
	
	private void setUILocale(){
		UIManager.put("OptionPane.yesButtonText", lang.nget("ui.yes"));
		UIManager.put("OptionPane.noButtonText", lang.nget("ui.no"));
		UIManager.put("OptionPane.cancelButtonText", lang.nget("ui.cancel"));
	}
	
	public static void initFontSize() {
		try{
			UIDefaults defaults = UIManager.getDefaults();
			
			int minSize = 12, maxSize = 14;
		
			for (Enumeration<?> e = defaults.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				Object value = defaults.get(key);
				
				if (value instanceof Font) {					
					Font font = (Font) value;
					int size = font.getSize();
					
					if(size < minSize) size = minSize;
					else if(size > maxSize) size = maxSize;
					
					if (value instanceof FontUIResource) {
						defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), size));
					} else {
						defaults.put(key, new Font(font.getName(), font.getStyle(), size));
					}
				}
			}
		} catch(Exception e){ log("Cannot change font sizes!", e); }
	}
	
	public static void initLookAndFeel(){
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log("Can't set system look and feel.");
			e.printStackTrace();
		}
	}
	
	public static void updateContainer(Container container, boolean deep){
		for(Component c : container.getComponents()){
			if(c instanceof LocalizableComponent) ((LocalizableComponent) c).updateLocale();
			if(c instanceof Container && deep) updateContainer((Container) c, true);
		}
	}
	
	public static List<Image> getFavicons(){		
		if(!favicons.isEmpty()) return Collections.unmodifiableList(favicons);
		
		int[] sizes = new int[]{256, 128, 96, 64, 48, 32, 24, 16};
		String loaded = "";
		
		for(int i : sizes){
			Image image = ImageCache.getImage("fav" + i + ".png", false);
			if(image == null) continue;
			
			loaded += ", " + i + "px";
			favicons.add(image);
		}
		
		if(loaded.isEmpty())
			log("No favicon is loaded.");
		else
			log("Favicons loaded:", loaded.substring(2));
		
		return favicons;
	}
	
	public static URL getRes(String uri){
		return TLauncherFrame.class.getResource(uri);
	}
	
	private static void log(Object...o){ U.log("[Frame]", o); }
}
