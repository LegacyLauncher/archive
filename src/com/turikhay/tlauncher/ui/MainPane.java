package com.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Point;

import com.turikhay.tlauncher.ui.backgrounds.Background;
import com.turikhay.tlauncher.ui.backgrounds.DefaultBackground;
import com.turikhay.tlauncher.ui.progress.DownloaderProgress;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.scenes.PseudoScene;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class MainPane extends ExtendedLayeredPane {
	private static final long serialVersionUID = -8854598755786867602L;
	
	private final TLauncherFrame rootFrame;
	
	private Background background;
	private PseudoScene scene;
	
	private final ConnectionWarning warning;
	private final DownloaderProgress progress;
	
	public final DefaultBackground defaultBackground;
	
	public final DefaultScene defaultScene;
	public final AccountEditorScene accountEditor;
	
	MainPane(TLauncherFrame frame){
		super(null); // TLauncherFrame will determine MainPane size with layout manager
		
		this.rootFrame = frame;
		
		this.background = this.defaultBackground = new DefaultBackground(this);
		this.add(defaultBackground);
		
		this.defaultScene = new DefaultScene(this);
		this.add(defaultScene);
		
		this.accountEditor = new AccountEditorScene(this);
		this.add(accountEditor);
		
		this.progress = new DownloaderProgress(frame);
		this.add(progress);
		
		this.warning = new ConnectionWarning();
		this.warning.setLocation(10, 10);
		this.add(warning);
		
		this.setScene(defaultScene, false);
	}
	
	public void showBackground(){
		background.setShown(true);
	}
	
	public void hideBackground(){
		background.setShown(false);
	}
	
	public Background getBackgroundPane(){
		return background;
	}
	
	public void setBackgroundPane(Background background){
		if(background == null)
			throw new NullPointerException();
		
		if(this.background.equals(background))
			return;
		
		for(Component comp : getComponents()){
			if(!comp.equals(background) && comp instanceof Background)
				((Background) comp).setShown(false);
		}
		
		this.background = background;
		this.background.setShown(true);
	}
	
	public DefaultBackground getDefaultBackgroundPane(){
		return defaultBackground;
	}
	
	public PseudoScene getScene(){
		return scene;
	}
	
	public void setScene(PseudoScene scene){
		this.setScene(scene, true);
	}
	
	public void setScene(PseudoScene scene, boolean animate){
		if(scene == null)
			throw new NullPointerException();
		
		if(scene.equals(this.scene))
			return;
		
		for(Component comp : getComponents())
			if(!comp.equals(scene) && comp instanceof PseudoScene)
				((PseudoScene) comp).setShown(false, animate);
		
		this.scene = scene;
		this.scene.setShown(true);
	}
	
	public void openDefaultScene(){
		setScene(defaultScene);
	}
	
	public void openAccountEditor(){
		setScene(accountEditor);
	}
	
	public TLauncherFrame getRootFrame(){
		return rootFrame;
	}
	
	public void onResize(){
		super.onResize();
		
		progress.setBounds(
			0,
			getHeight() - DownloaderProgress.DEFAULT_HEIGHT,
			getWidth(),
			DownloaderProgress.DEFAULT_HEIGHT);
	}
	
	/**
	 * Location of some components can be determined only with <code>getLocationOnScreen()</code> method.
	 * This method should help to find out the location of a <code>Component</code> on the <code>MainPane</code>.
	 * 
	 */
	public Point getLocationOf(Component comp){
		Point
			compLocation = comp.getLocationOnScreen(),
			paneLocation = getLocationOnScreen();
		
		return new Point(compLocation.x - paneLocation.x, compLocation.y - paneLocation.y);
	}
}
