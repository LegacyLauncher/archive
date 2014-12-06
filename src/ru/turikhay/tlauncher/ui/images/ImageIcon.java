package ru.turikhay.tlauncher.ui.images;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.JLabel;

public class ImageIcon implements Icon {
	private transient Image image;
	private int width, height;

	private DisabledImageIcon disabledInstance;

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

	public DisabledImageIcon getDisabledInstance() {
		if(disabledInstance == null)
			disabledInstance = new DisabledImageIcon();

		return disabledInstance;
	}

	public class DisabledImageIcon implements Icon {
		private float disabledOpacity;
		private AlphaComposite opacityComposite;

		private DisabledImageIcon() {
			setDisabledOpacity(0.5f);
		}

		public float getDisabledOpacity() {
			return disabledOpacity;
		}

		public void setDisabledOpacity(float f) {
			this.disabledOpacity = f;
			this.opacityComposite = AlphaComposite.
					getInstance(AlphaComposite.SRC_OVER, disabledOpacity);
		}

		@Override
		public void paintIcon(Component c, Graphics g0, int x, int y) {
			if(image == null)
				return;

			Graphics2D g = (Graphics2D) g0;
			Composite oldComposite = g.getComposite();

			g.setComposite(opacityComposite);
			g.drawImage(image, x, y, width, height, null);
			g.setComposite(oldComposite);
		}

		@Override
		public int getIconWidth() {
			return ImageIcon.this.getIconWidth();
		}

		@Override
		public int getIconHeight() {
			return ImageIcon.this.getIconHeight();
		}
	}

	public static ImageIcon setup(JLabel label, ImageIcon icon) {
		if(label == null)
			return null;

		label.setIcon(icon);

		if(icon != null)
			label.setDisabledIcon(icon.getDisabledInstance());

		return icon;
	}
}
