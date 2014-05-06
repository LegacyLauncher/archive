package ru.turikhay.tlauncher.ui.images;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;

public class ImageIcon implements Icon {
	private transient Image image;
	private int width, height;
	
	public ImageIcon(Image image, int width, int height) {
		this.setImage(image, width, height);
	}
	
	public ImageIcon(Image image) {
		this(image, 0, 0);
	}
	
	public void setImage(Image image, int preferredWidth, int preferredHeight) {
		if(image == null) {
			this.image = null;
			return;
		}
		
		int realWidth = image.getWidth(null), realHeight = image.getHeight(null);
		
		this.width = preferredWidth > 0? preferredWidth : realWidth;
		this.height = preferredHeight > 0? preferredHeight : realHeight;
		
		Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		
		this.image = scaled;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		if(image == null)
			return;
		
		g.drawImage(image, x, y, width, height, null);
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

}
