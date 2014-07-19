package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentEvent;

import ru.turikhay.tlauncher.ui.background.Background;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;

public class SlideBackground extends Background {

	private static final long serialVersionUID = -4479685866688951989L;

	private final SlideBackgroundThread thread;

	final BackgroundHolder holder;
	final ExtendedComponentAdapter listener;

	private Image oImage;
	private int oImageWidth, oImageHeight;

	private Image vImage;
	private int vImageWidth, vImageHeight;

	public SlideBackground(BackgroundHolder holder) {
		super(holder, Color.black);

		this.holder = holder;
		this.thread = new SlideBackgroundThread(this);

		this.thread.setSlide(thread.defaultSlide, false); // Set up as fallback.
		this.thread.refreshSlide(false); // Refresh slide from configuration file.

		this.listener = new ExtendedComponentAdapter(this, 1000) {			
			@Override
			public void onComponentResized(ComponentEvent e) {
				updateImage();
				repaint();
			}			
		};
		this.addComponentListener(listener);
	}

	public SlideBackgroundThread getThread() {
		return thread;
	}

	public Image getImage() {
		return oImage;
	}

	public void setImage(Image image) {
		if (image == null)
			throw new NullPointerException();

		this.oImage = image;
		this.oImageWidth = image.getWidth(null);
		this.oImageHeight = image.getHeight(null);

		this.updateImage();
	}

	private void updateImage() {
		double windowWidth = getWidth(), windowHeight = getHeight();

		double ratio = Math.min(oImageWidth / windowWidth, oImageHeight / windowHeight);
		double width, height;

		if (ratio < 1) {
			// Oh shi~, this guy has really huge screen. Or the image is too
			// small.
			width = oImageWidth;
			height = oImageHeight;
		} else {
			width = oImageWidth / ratio;
			height = oImageHeight / ratio;
		}

		vImageWidth = (int) width;
		vImageHeight = (int) height;

		if(vImageWidth == 0 || vImageHeight == 0)
			vImage = null;

		else if(oImageWidth == vImageWidth && oImageHeight == vImageHeight)
			vImage = oImage;

		else vImage = oImage.getScaledInstance(vImageWidth, vImageHeight, Image.SCALE_SMOOTH);
	}

	@Override
	public void paintBackground(Graphics g) {
		if(vImage == null) 
			updateImage();

		if(vImage == null)
			return;

		double
		windowWidth = getWidth(),
		windowHeight = getHeight(),

		ratio = Math.min(vImageWidth / windowWidth, vImageHeight / windowHeight),
		width = vImageWidth / ratio, height = vImageHeight / ratio;

		double			
		x = (windowWidth - width) / 2,
		y = (windowHeight - height) / 2;

		g.drawImage(vImage, (int) x, (int) y, (int) width, (int) height, null);
	}

}
