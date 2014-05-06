package ru.turikhay.tlauncher.ui.images;

import java.awt.Image;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import ru.turikhay.tlauncher.exceptions.TLauncherException;

public class ImageCache {
	private final static boolean THROW_IF_ERROR = true;
	private final static Map<URL, Image> imageCache = Collections
			.synchronizedMap(new HashMap<URL, Image>());

	public static Image loadImage(URL url, boolean throwIfError) {
		if (url == null)
			throw new NullPointerException("URL is NULL");

		try {
			Image image = ImageIO.read(url);
			imageCache.put(url, image);

			return image;
		} catch (Exception e) {
			if (throwIfError)
				throw new TLauncherException("Cannot load required image: "
						+ url, e);
			else
				e.printStackTrace();
		}

		return null;
	}

	public static Image loadImage(URL url) {
		return loadImage(url, THROW_IF_ERROR);
	}

	public static Image getImage(String uri, boolean throwIfError) {
		return loadImage(getRes(uri), throwIfError);
	}

	public static Image getImage(String uri) {
		return getImage(uri, THROW_IF_ERROR);
	}
	
	public static ImageIcon getIcon(String uri, int width, int height, boolean throwIfError) {
		return new ImageIcon(getImage(uri, throwIfError), width, height);
	}
	
	public static ImageIcon getIcon(String uri, int width, int height) {
		return getIcon(uri, width, height, THROW_IF_ERROR);
	}
	
	public static ImageIcon getIcon(String uri, int widthNheight) {
		return getIcon(uri, widthNheight, widthNheight, THROW_IF_ERROR);
	}
	
	public static ImageIcon getIcon(String uri) {
		return getIcon(uri, 0, 0);
	}

	public static URL getRes(String uri) {
		if (uri == null)
			return null;
		
		return ImageCache.class.getResource(uri);
	}
}
