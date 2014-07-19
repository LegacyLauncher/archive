package ru.turikhay.tlauncher.ui.background.slide;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.explorer.ImageFileFilter;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class SlideBackgroundThread extends LoopedThread {
	private static final Pattern extensionPattern = ImageFileFilter.extensionPattern;
	private static final String defaultImageName = "plains.jpg";

	private final SlideBackground background;

	final Slide defaultSlide;
	private Slide currentSlide;

	SlideBackgroundThread(SlideBackground background) {
		super("SlideBackgroundThread");

		this.background = background;
		this.defaultSlide = new Slide(ImageCache.getRes(defaultImageName));

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
		this.iterate();
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
		
		U.sleepFor(500); // Let them wait, hahahahahahaha
		
		background.holder.cover.removeCover(animate);
	}
	
	@Override
	protected void iterateOnce() {
		refreshSlide(true);
	}

	private URL getImageURL(String path) {
		log("Trying to resolve path:", path);

		if (path == null) {
			log("Na NULL i suda NULL.");
			return null;
		}
		
		URL asURL = U.makeURL(path);
		if(asURL != null) {
			log("Path resolved as an URL:", asURL);
			return asURL;
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
