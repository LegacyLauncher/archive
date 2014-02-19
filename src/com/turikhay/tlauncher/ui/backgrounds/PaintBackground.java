package com.turikhay.tlauncher.ui.backgrounds;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.VolatileImage;

import com.turikhay.tlauncher.ui.MainPane;

public abstract class PaintBackground extends Background {
	private static final long serialVersionUID = 1251234865840478018L;
	
	protected int width, height;
	protected double relativeSize = 1;
	protected VolatileImage vImage;

	public PaintBackground(MainPane main) {
		super(main);
	}
	
	public void update(Graphics g0){
		super.update(g0);
	}
	
	public void paint(Graphics g0){
		//super.paint(g0);
		g0.drawImage(draw(g0), 0, 0, getWidth(), getHeight(), null);
	}
	
	public VolatileImage draw(Graphics g0){
		int iw = getWidth(), w = (int) (iw * relativeSize), ih = getHeight(), h = (int) (ih * relativeSize);
		boolean force = w != width || h != height;
		
		width = w; height = h;
		if(vImage == null || vImage.getWidth() != w || vImage.getHeight() != h)
			vImage = createVolatileImage(w, h);
		
		Graphics2D g = (Graphics2D) vImage.getGraphics();
		
		this.draw(g, force);
		
		return vImage;
	}
	
	protected abstract void draw(Graphics2D g, boolean force);
}
