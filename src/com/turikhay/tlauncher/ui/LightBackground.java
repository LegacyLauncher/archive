package com.turikhay.tlauncher.ui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.turikhay.tlauncher.util.U;

public class LightBackground extends Background {
	protected BufferedImage
		grass = loadImage("grass.png"),
		sun = loadImage("sun.png");
	
	protected final int
		grassW, grassH, sunW, sunH;
	protected double sunLocation;
	
	public LightBackground(DecoratedPanel comp, double loc) {
		super(comp);
		
		grassW = grass.getWidth(null);
		grassH = grass.getHeight(null);
		
		sunW = sun.getWidth(null);
		sunH = sun.getHeight(null);
		
		relativeSize = 0.5; 
		
		sunLocation = (loc <= 0)? U.doubleRandom() : loc;
		if(sunLocation <= 0) sunLocation += 0.5;
	}

	public void draw(Graphics2D g, boolean force) {
		if(force)
			drawGrass(g);
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
			y = (int) (height - grassH - sunH);
		g.drawImage(sun, x, y, sunW, sunH, null);
		
		g.setColor(comp.getBackground());
		
		g.fillRect(0, 0, x, height - grassH);
		g.fillRect(0, 0, width, y);
		g.fillRect(x + sunW, y, sunW, sunH);
	}

}
