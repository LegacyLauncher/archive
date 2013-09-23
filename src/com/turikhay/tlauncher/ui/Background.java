package com.turikhay.tlauncher.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.turikhay.tlauncher.util.U;

public abstract class Background {
	private static Map<String, BufferedImage> loaded = new HashMap<String, BufferedImage>();
	
	final DecoratedPanel comp;
	int width, height;
	double relativeSize = 1;
	VolatileImage vImage;
	
	Background(DecoratedPanel comp){
		this.comp = comp;
	}
	public VolatileImage draw(Graphics g0){
		int iw = comp.getWidth(), w = (int) (iw * relativeSize), ih = comp.getHeight(), h = (int) (ih * relativeSize);
		boolean force = w != width || h != height;
		
		width = w; height = h;
		if(vImage == null || vImage.getWidth() != w || vImage.getHeight() != h)
			vImage = comp.createVolatileImage(w, h);
		
		Graphics2D g = (Graphics2D) vImage.getGraphics();
		
		this.draw(g, force);
		
		return vImage;
	}
	protected static BufferedImage loadImage(String name){
		if(loaded.containsKey(name))
			return loaded.get(name);
		
		try {
			Image i = ImageIO.read(TLauncherFrame.class.getResource(name));
			int w = i.getWidth(null), h = i.getHeight(null);
			
			BufferedImage bi = new BufferedImage(w, h, BufferedImage.SCALE_DEFAULT);
			bi.getGraphics().drawImage(i, 0, 0, null);
			
			loaded.put(name, bi);
			return bi;
		} catch (IOException e) {
			U.log("Cannot load required image", e);
		}
		
		return null;
	}
	protected abstract void draw(Graphics2D g, boolean force);
}
