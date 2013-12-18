package com.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Button;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;

import com.turikhay.tlauncher.exceptions.TLauncherException;
import com.turikhay.tlauncher.ui.ImageButton.ImageRotation;

public class SearchButton extends Button implements Blockable {
	private static final long serialVersionUID = 682875580116075167L;
	
	protected Image image;
	protected ImageRotation rotation = ImageRotation.CENTER;
	protected int margin = 4;
	
	private boolean pressed;
		
	SearchButton(final SearchPanel sp){
		this.image = loadImage("search.png");
		
		this.setForeground(Color.white);
		this.setBackground(Color.black);
		
		this.initListeners();
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { sp.search(); }
		});
		
		this.setPreferredSize(new Dimension(50, getHeight()));
	}
	
	public void update(Graphics g){
		super.update(g);
		this.paint(g);
	}
	public void paint(Graphics g0){
		super.paint(g0);
		if(image == null) return;
		
		Graphics2D g = (Graphics2D) g0;
		
		String text = getLabel();
		boolean drawtext = text != null && text.length() > 0;
		FontMetrics fm = g.getFontMetrics();
		
		float opacity = (isEnabled())? 1.0F : .5F;
		int width = getWidth(), height = getHeight(), rmargin = margin;
		int offset = (pressed)? 1 : 0;
		int iwidth = image.getWidth(null), iheight = image.getHeight(null), twidth;
		int ix = 0, iy = height / 2 - iheight / 2;
		
		if(drawtext)
			twidth = fm.stringWidth(text);
		else
			twidth = rmargin = 0;
		
		switch(rotation){
		case LEFT:
			ix = width / 2 - twidth / 2 - iwidth - rmargin;
			break;
		case CENTER:
			ix = width / 2 - iwidth / 2;
			break;
		case RIGHT:
			ix = width / 2 + twidth / 2 + rmargin;
			break;
		default:
			throw new IllegalStateException("Unknown rotation!");
		}
		Composite c = g.getComposite(); g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g.drawImage(image, ix + offset, iy + offset, null); g.setComposite(c);
		
		pressed = false;
	}
	
	protected static Image loadImage(String path){
		try{ return ImageIO.read(SupportButton.class.getResource(path)); }
		catch(Exception e){
			throw new TLauncherException("Cannot load required image ("+path+")!", e);
		}
	}
	
	private void initListeners(){
		this.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) { pressed = true; }
			public void mouseReleased(MouseEvent e) {}
		});
		
		this.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) { if(e.getKeyCode() != 32) return; pressed = true; }
			public void keyReleased(KeyEvent e) { pressed = false; }
			public void keyTyped(KeyEvent e) {}
		});
	}
	
	public void block(Object reason) {
		this.setEnabled(false);
	}

	public void unblock(Object reason) {
		this.setEnabled(true);
	}
}
