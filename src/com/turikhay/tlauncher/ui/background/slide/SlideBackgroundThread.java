package com.turikhay.tlauncher.ui.background.slide;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.explorer.ImageFileFilter;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import com.turikhay.util.async.ExtendedThread;

public class SlideBackgroundThread extends ExtendedThread {
	private static final String REFRESH_BLOCK = "refresh";
	private static final Pattern extensionPattern = ImageFileFilter.extensionPattern;

	private final SlideBackground background;

	final Slide defaultSlide;
	private Slide currentSlide;

	SlideBackgroundThread(SlideBackground background) {
		super("SlideBackgroundThread");

		this.background = background;
		this.defaultSlide = new Slide(ImageCache.getRes("skyland.jpg"));

		this.startAndWait();
	}

	public SlideBackground getBackground() {
		return background;
	}

	public Slide getSlide() {
		return currentSlide;
	}

	public synchronized void refreshSlide(boolean animate) {
		String path = TLauncher.getInstance().getSettings()
				.get("gui.background");
		URL url = getImageURL(path);
		Slide slide = url == null ? defaultSlide : new Slide(url);

		setSlide(slide, animate);
	}

	public void asyncRefreshSlide() {
		unblockThread(REFRESH_BLOCK);
	}

	public synchronized void setSlide(Slide slide, boolean animate) {
		if (slide == null)
			throw new NullPointerException();

		if (slide.equals(currentSlide))
			return;

		Image image = slide.getImage();

		if (image == null) {
			slide = defaultSlide;
			image = slide.getImage();
		}

		this.currentSlide = slide;
		
		if(image == null) {
			log("Default image is NULL. Check accessibility to the JAR file of TLauncher.");
			return;
		}

		background.holder.cover.makeCover(animate);
		background.setImage(image);
		background.holder.cover.removeCover(animate);
	}

	@Override
	public void run() {
		while (true) {
			blockThread(REFRESH_BLOCK);
			refreshSlide(true);
		}
	}

	private URL getImageURL(String path) {
		log("Trying to resolve path:", path);

		if (path == null) {
			log("Na NULL i suda NULL.");
			return null;
		}

		File asFile = new File(path);
		if (asFile.isFile()) {
			String absPath = asFile.getAbsolutePath();
			log("Path resolved as a file:", absPath);

			String ext = FileUtil.getExtension(asFile);
			if (ext == null || !extensionPattern.matcher(ext).matches()) {
				log("This file doesn't seem to be an image. It should have JPG or PNG format.");
				return null;
			}

			try {
				return asFile.toURI().toURL();
			} catch (IOException e) {
				log("Cannot covert this file into URL.", e);
				return null;
			}
		}

		log("Cannot resolve this path.");
		return null;
	}

	protected void log(Object... w) {
		U.log("[" + getClass().getSimpleName() + "]", w);
	}
}
