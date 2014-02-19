package com.turikhay.tlauncher.ui.backgrounds;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.U;

public abstract class LightBackground extends PaintBackground {
	private static final long serialVersionUID = -3732711088655124975L;

	protected Image
		grass = ImageCache.getImage("grass.png"),
		sun = ImageCache.getImage("sun.png"),
		glow = ImageCache.getImage("glow.png");
	
	protected final int
		grassW, grassH, sunW, sunH, glowW, glowH;
	protected double sunLocation;
	protected Color backgroundColor = new Color(141, 189, 233);
	
	public LightBackground(MainPane main, double loc) {
		super(main);
		
		setBackground(backgroundColor);
		
		grassW = grass.getWidth(null);
		grassH = grass.getHeight(null);
		
		sunW = sun.getWidth(null);
		sunH = sun.getHeight(null);
		
		glowW = glow.getWidth(null);
		glowH = glow.getHeight(null);
		
		relativeSize = .5;
		
		sunLocation = (loc <= 0)? U.doubleRandom() : loc;
		if(sunLocation <= 0) sunLocation += 0.5;
	}

	public void draw(Graphics2D g, boolean force) {		
		if(force)
			drawGrass(g);
		
		drawGlow(g);
		drawSun(g);
	}
	
	public void drawGrass(Graphics2D g){
		for(int rw = 0; rw <= width;) {
			g.drawImage(grass, rw, height - grassH, null);
			
			rw += grassW;
		}
	}
	
	public void drawSun(Graphics2D g){		
		int
			x = (int) (width * sunLocation - sunW / 2), //((Math.abs((sunW - width)) * (sunLocation + 1)) / 2), //(width / 2 - sunW / 2 + (width / 2 - sunW / 2) * sunLocation),
			y = height - grassH - sunH;
		g.drawImage(sun, x, y, sunW, sunH, null);
		
		g.setColor(backgroundColor);
		
		g.fillRect(0, 0, x, height - grassH - glowH);
		g.fillRect(0, 0, width, y);
		g.fillRect(x + sunW, y, sunW, sunH - glowH);
	}
	
	
	public void drawGlow(Graphics2D g){
		for(int rw = 0; rw <= width;) {
			g.drawImage(glow, rw, height - grassH - glowH, null);
			
			rw += glowW;
		}
	}
}
