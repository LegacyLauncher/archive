package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import ru.turikhay.tlauncher.ui.background.Background;
import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.util.U;

public class SlideBackground extends Background {
	private static final long serialVersionUID = -4479685866688951989L;

	private final SlideBackgroundThread thread;
	final BackgroundHolder holder;

	private Image image;
	private double imageWidth, imageHeight;

	public SlideBackground(BackgroundHolder holder) {
		super(holder, Color.black);

		this.holder = holder;
		this.thread = new SlideBackgroundThread(this);

		this.thread.setSlide(thread.defaultSlide, false); // Set up as fallback.
		this.thread.refreshSlide(false); // Refresh slide from configuration file.
	}

	public SlideBackgroundThread getThread() {
		return thread;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		if (image == null)
			throw new NullPointerException();

		this.image = image;
		this.imageWidth = image.getWidth(null);
		this.imageHeight = image.getHeight(null);
	}

	@Override
	public void paintBackground(Graphics g) {
		double windowWidth = getWidth(), windowHeight = getHeight();

		double ratio = Math.min(imageWidth / windowWidth, imageHeight / windowHeight);
		double width, height;

		if (ratio < 1) {
			// Oh shi~, this guy has really huge screen. Or the image is too
			// small.
			width = imageWidth;
			height = imageHeight;
		} else {
			width = imageWidth / ratio;
			height = imageHeight / ratio;
		}

		double
			x = (windowWidth - width) / 2,
			y = (windowHeight - height) / 2;

		g.drawImage(image, (int) x, (int) y, (int) width, (int) height, null);
	}

	protected void log(Object... w) {
		U.log("[" + getClass().getSimpleName() + "]", w);
	}
}
