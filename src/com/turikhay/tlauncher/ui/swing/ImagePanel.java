package com.turikhay.tlauncher.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import com.turikhay.util.U;

public class ImagePanel extends ExtendedPanel {
	private static final long serialVersionUID = 1L;
	public static final float DEFAULT_ACTIVE_OPACITY = 1.0F, DEFAULT_NON_ACTIVE_OPACITY = 0.75F;
	
	protected Object animationLock = new Object();
	protected Image image;
	protected float activeOpacity, nonActiveOpacity;
	protected boolean antiAlias;
	
	protected int timeFrame;
	protected float opacity;
	protected boolean hover, shown, animating;
	
	public ImagePanel(String image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias){
		this(ImageCache.getImage(image), activeOpacity, nonActiveOpacity, shown, antiAlias);
	}
	
	public ImagePanel(Image image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias){
		this.setImage(image);
		
		this.setActiveOpacity(activeOpacity);
		this.setNonActiveOpacity(nonActiveOpacity);
		
		this.setAntiAlias(antiAlias);
		
		this.shown = shown;		
		this.opacity = (shown)? nonActiveOpacity : 0.0F;
		this.timeFrame = 10;
		
		this.setBackground(new Color(0, 0, 0, 0));
		
		this.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				onClick();
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}

			public void mouseEntered(MouseEvent e) {
				onMouseEntered();
			}
			public void mouseExited(MouseEvent e) {
				onMouseExited();
			}
		});
	}
	
	public void setImage(Image image, boolean resetSize){
		if(image == null)
			throw new NullPointerException();
		
		synchronized(animationLock){
			this.image = image;
			this.setSize(image.getWidth(null), image.getHeight(null));
		}
	}
	public void setImage(Image image){ setImage(image, true); }
	
	public void setActiveOpacity(float opacity){
		if(opacity > 1.0F || opacity < 0.0F)
			throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: "+opacity+") <= 1.0F");
		
		this.activeOpacity = opacity;
	}
	
	public void setNonActiveOpacity(float opacity){
		if(opacity > 1.0F || opacity < 0.0F)
			throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: "+opacity+") <= 1.0F");
		
		this.nonActiveOpacity = opacity;
	}
	
	public void setAntiAlias(boolean set){ this.antiAlias = set; }
	
    public void paintComponent(Graphics g0) {
    	Graphics2D g = (Graphics2D) g0;
    	Composite oldComp = g.getComposite();
    	
    	if(this.antiAlias) g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    	g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    	
    	g.setComposite(oldComp);
    }
    
	public void show(){
		if(shown) return; shown = true;
		
		synchronized(animationLock){
			animating = true;
			this.setVisible(true);
			opacity = 0.0F;
			
			float selectedOpacity = (hover)? activeOpacity : nonActiveOpacity;
			
			while(opacity < selectedOpacity){
				opacity += 0.01F;
				if(opacity > selectedOpacity) opacity = selectedOpacity;
				
				this.repaint();				
				U.sleepFor(timeFrame);
			}
			
			animating = false;
		}
	}
	
	public void hide(){
		if(!shown) return; shown = false;
		
		synchronized(animationLock){
			animating = true;
			
			while(opacity > 0.0F){
				opacity -= 0.01F;
				if(opacity < 0.0F) opacity = 0.0F;
				
				this.repaint();
				U.sleepFor(timeFrame);
			}
			
			this.setVisible(false);			
			animating = false;
		}
	}
	
    protected boolean onClick(){
    	return shown;
    }
    protected void onMouseEntered(){
    	this.hover = true;
    	
    	if(animating || !shown) return;
    	this.opacity = this.activeOpacity;
    	this.repaint();
    }
    protected void onMouseExited(){
    	this.hover = false;
    	
    	if(animating || !shown) return;
    	this.opacity = this.nonActiveOpacity;
    	this.repaint();
    }
}
