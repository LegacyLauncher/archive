package com.turikhay.tlauncher.ui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.image.VolatileImage;

public class MainContainer extends Panel {
	private static final long serialVersionUID = 8925486339442046362L;
	
	TLauncherFrame f;
	VolatileImage vImage;
	Sun sun;
	private boolean drawSun = true, textChanged = false;
	private int lagmeter = 0, ow, oh;
	
	final MainContainer instance = this;
	
	private String text = null;
	Font font;
	
	MainContainer(TLauncherFrame f){
		this.f = f;
		this.font = f.getFont();
		
		this.sun = new Sun(this);
		
		this.setBackground(f.bgcolor);
		this.setText(f.lang.get("hiddenText"));
		
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		
		this.add(f.lf);
	}
	
	public void update(Graphics g){
		this.paint(g);
	}
	
	public void paint(Graphics g0){
		int iw = getWidth(), w = iw / 2, ih = getHeight(), h = ih / 2;
		boolean force = drawSun || ow != iw || oh != ih;
		ow = iw; oh = ih;
		
		if(vImage == null || vImage.getWidth() != w || vImage.getHeight() != h) vImage = createVolatileImage(w, h);
		
		Graphics g = vImage.getGraphics();
		
		if(drawSun){
				long s = System.currentTimeMillis();
			sun.onPaint(w, h, g);
				long e = System.currentTimeMillis(), diff = e-s;
			
			if(diff > 1){
				if(lagmeter > 5){
					sun.cancel();
					
					f.lf.setError(f.lang.get("sun.stopped"));
					drawSun = false;
				} else ++lagmeter;
			}			
		}
		
		if(force){
			int rw = 0;
			for (int x = 0; x <= w / f.bgimage.getWidth(null); x++) {
				g.drawImage(f.bgimage, rw, h - f.bgimage.getHeight(null), null);
				
				rw += f.bgimage.getWidth(null);
			}
		}
		
		if(force || textChanged){
			if(text != null){
				g.setFont(font);
				g.drawString(text, 0, g.getFontMetrics().getAscent());
			}
			this.textChanged = false;
		}
		
		g0.drawImage(vImage, 0, 0, w * 2, h * 2, null);
	}
	
	public void setText(String text){ this.text = text; this.textChanged = true; }
	public String getText(){ return text; }
}
