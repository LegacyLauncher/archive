package com.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Button;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;

import com.turikhay.tlauncher.TLauncherException;

public class ImageButton extends Button {
	private static final long serialVersionUID = 1L;
	
	protected Image image;
	protected ImageRotation rotation;
	protected int margin = 4;
	
	private boolean pressed;
	
	protected ImageButton(){ this.initListeners(); }
	public ImageButton(String label, Image image, ImageRotation rotation, int margin){
		super(label);
		
		this.image = image;
		this.rotation = rotation;
		this.margin = margin;
		
		this.initListeners();
	}
	public ImageButton(String label, Image image, ImageRotation rotation){ this(label, image, rotation, 4); }
	public ImageButton(String label, Image image){ this(label, image, ImageRotation.CENTER); }
	public ImageButton(String label, String imagepath, ImageRotation rotation, int margin){ this(label, loadImage(imagepath), rotation, margin); }
	public ImageButton(String label, String imagepath, ImageRotation rotation){ this(label, loadImage(imagepath), rotation); }
	public ImageButton(String label, String imagepath){ this(label, loadImage(imagepath)); }
	
	public Image getImage(){ return image; }	
	public ImageRotation getRotation(){ return rotation; }
	public int getMargin(){ return margin; }
	
	public void update(Graphics g){
		this.paint(g);
	}
	public void paint(Graphics g0){
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
			public void keyPressed(KeyEvent e) { if(e.getExtendedKeyCode() != 32) return; pressed = true; }
			public void keyReleased(KeyEvent e) { pressed = false; }
			public void keyTyped(KeyEvent e) {}
		});
	}
	
	public enum ImageRotation {
		LEFT, CENTER, RIGHT
	}
}
