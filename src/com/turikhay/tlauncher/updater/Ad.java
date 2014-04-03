package com.turikhay.tlauncher.updater;

import java.net.URL;

import com.turikhay.tlauncher.configuration.SimpleConfiguration;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.U;

public class Ad {
	private static final String[] RANDOM_CHARS = { "creeper", "sheep",
			"skeleton", "steve", "wither", "zombie" };

	private String content;
	private final int[] size;
	private final URL image;

	private Ad(SimpleConfiguration configuration) {
		this.content = configuration.get("ad.content");

		if (content == null)
			throw new NullPointerException();

		this.size = IntegerArray.toArray(configuration.get("ad.size"));

		if (size.length != 2)
			throw new IllegalArgumentException("Invalid length of size array:"
					+ size.length);

		this.image = getInternal(configuration.get("ad.image"));
	}

	public String getContent() {
		return content;
	}

	public int[] getSize() {
		return size;
	}

	public URL getImage() {
		return image;
	}

	private static URL getInternal(String path) {
		if (path == null)
			return null;
		if (path.equals("random"))
			path = U.getRandom(RANDOM_CHARS) + ".png";

		return ImageCache.getRes(path);
	}

	static Ad parseFrom(SimpleConfiguration configuration) {
		try {
			return new Ad(configuration);
		} catch (RuntimeException e) {
			return null;
		}
	}
}
