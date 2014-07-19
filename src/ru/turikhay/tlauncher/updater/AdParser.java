package ru.turikhay.tlauncher.updater;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class AdParser {
	public static final int SIZE_ARR_LENGTH = 2;

	private static final char
	KEY_DELIMITER = '.', VALUE_DELIMITER = '|', ESCAPE = '\\', SIZE_DELIMITER = 'x';
	private static final String
	AD_PREFIX = "ad" + KEY_DELIMITER, BASE64_IND = "data:image", COPY_IND = "copy:";

	private final Map<String, Ad> ads;

	private AdParser() {
		this.ads = new Hashtable<String, Ad>();
	}

	private AdParser(SimpleConfiguration c) throws RuntimeException {
		this();

		if(c == null)
			throw new NullPointerException("configuration");

		for(String key : c.getKeys()) {
			if(!key.startsWith(AD_PREFIX)) continue;

			Ad ad;

			try {
				ad = parsePair(key, c.get(key));
			}catch(RuntimeException re) {
				log("Error parsing key:", key, re);
				continue;
			}

			if(ad == null)
				throw new RuntimeException("Well... what the...? ("+ key +")");

			ads.put(ad.getLocale(), ad);
		}

		for(Ad ad : ads.values()) {
			String imageCopy;

			try { imageCopy = needCopy(ad.getImage()); }
			catch(RuntimeException re) {
				// log("Error parsing image copy indicator for", ad, re);
				continue;
			}

			if(imageCopy != null) {
				Ad copyAd = ads.get(imageCopy);

				if(copyAd == null)
					log("Cannot find copy ad indicated by image in", ad);				
				else
					ad.setImage(copyAd.getImage());
			}
		}
	}

	public Ad get(String locale) {
		return ads.get(locale);
	}

	private Ad parsePair(String key, String value) throws RuntimeException {
		String parsing = key.substring(AD_PREFIX.length());

		if(parsing.isEmpty())
			throw new IllegalArgumentException("cannot determine locale");

		// Parsing locale
		String locale = seek(parsing, 0, KEY_DELIMITER);

		// Parsing type
		String tempType = seek(value, 0);
		AdType type;

		try {
			type = Reflect.parseEnum0(AdType.class, tempType);
		}catch(ParseException pe) {
			type = null;
		}

		int caret = tempType.length();

		// Preparing content string
		String content;

		if(type == null) {// cannot parse type: count it as content
			content = tempType;
			type = AdType.DEFAULT;
		} else {
			// Parsing content
			content = seek(value, caret);
			caret += content.length();
		}

		// Parsing size
		String tempSize = seek(value, ++caret);
		IntegerArray sizeArray;

		try { sizeArray = IntegerArray.parseIntegerArray(tempSize, SIZE_DELIMITER); }
		catch(RuntimeException e) {
			throw new ParseException("Cannot parse size: \""+ tempSize +"\"", e);
		}

		if(sizeArray.size() != SIZE_ARR_LENGTH)
			throw new IllegalArgumentException("illegal size array length");

		int[] size = sizeArray.toArray();

		caret += tempSize.length();

		// Parsing image
		String tempImage = seek(value, ++caret);
		String image = tempImage.isEmpty()? null : parseImage(tempImage);

		return new Ad(locale, type, content, size, image);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder
		.append(getClass().getSimpleName())
		.append("{ads=")
		.append(U.toLog(ads))
		.append('}');

		return builder.toString();
	}

	private static String seek(String parsing, int caret, char delimiter) {
		StringBuilder builder = new StringBuilder();

		char lastchar = '\0';
		char c;

		while(caret < parsing.length()) {
			c = parsing.charAt(caret);
			++caret;

			if(c == delimiter && lastchar != ESCAPE)
				break;

			builder.append(c);
			lastchar = c;
		}

		return builder.toString();
	}

	private static String seek(String parsing, int caret) {
		return seek(parsing, caret, VALUE_DELIMITER);
	}

	private static String needCopy(String value) {
		if(value.startsWith(COPY_IND)) {
			String valueKey = value.substring(COPY_IND.length());

			if(valueKey.isEmpty())
				throw new IllegalArgumentException("copy key is null");

			return valueKey;
		}
		return null;
	}

	public class Ad {
		private final String locale;

		private final AdType type;
		private final String content;
		private final int[] size;

		private String image;

		private Ad(String locale, AdType type, String content, int[] size, String image) {
			if(locale == null)
				throw new NullPointerException("locale");

			if(locale.isEmpty())
				throw new IllegalArgumentException("locale is empty");

			if(type == null)
				throw new NullPointerException("type");

			if(content == null)
				throw new NullPointerException("content");

			if(content.isEmpty())
				throw new IllegalArgumentException("content is empty");

			if(size == null)
				throw new NullPointerException("size");

			if(size.length != SIZE_ARR_LENGTH)
				throw new IllegalArgumentException("size array has illegal length:"+ size.length +" (instead of "+ SIZE_ARR_LENGTH +")");

			this.locale = locale;
			this.type = type;
			this.content = content;
			this.size = size;

			this.image = image;
		}

		public String getLocale() {
			return locale;
		}

		public AdType getType() {
			return type;
		}

		public String getContent() {
			return content;
		}

		public String getImage() {
			return image;
		}

		void setImage(String image) {
			this.image = image;
		}

		public int getWidth() {
			return size[0];
		}

		public int getHeight() {
			return size[1];
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			builder
			.append(getClass().getSimpleName())
			.append("{")
			.append("locale=").append(locale).append(';')
			.append("type=").append(type).append(';')
			.append("size=").append(size[0]).append(SIZE_DELIMITER).append(size[1]).append(';')
			.append("content=\"");

			if(content.length() < 50)
				builder.append(content);
			else
				builder
				.append(content.substring(0, 46))
				.append("...");

			builder
			.append("\";")
			.append("image=");

			if(image != null && image.length() > 24)
				builder
				.append(image.substring(0, 22))
				.append("...");
			else
				builder.append(image);

			builder.append('}');

			return builder.toString();
		}
	}

	public enum AdType {
		DEFAULT;
	}

	static AdParser parseFrom(SimpleConfiguration configuration) {
		try {
			return new AdParser(configuration);
		} catch (RuntimeException e) {
			U.log(e);
			return null;
		}
	}

	static String parseImage(String image) {
		if(image == null)
			return null;

		if(image.startsWith(BASE64_IND) || image.startsWith(COPY_IND))
			return image;

		URL url = ImageCache.getRes(image);
		return url == null? null : url.toString();
	}

	private static void log(Object... o) { U.log("[AdParser]", o); }
}
