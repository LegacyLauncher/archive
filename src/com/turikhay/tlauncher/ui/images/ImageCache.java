package com.turikhay.tlauncher.ui.images;

import java.awt.Image;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.turikhay.tlauncher.exceptions.TLauncherException;

public class ImageCache {
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
		return loadImage(url, true);
	}

	public static Image getImage(String uri, boolean throwIfError) {
		return loadImage(getRes(uri), throwIfError);
	}

	public static Image getImage(String uri) {
		return getImage(uri, true);
	}

	public static URL getRes(String uri) {
		if (uri == null)
			return null;
		return ImageCache.class.getResource(uri);
	}
}
