package ru.turikhay.tlauncher.updater;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.U;

public final class AdParser {
	public static final int SIZE_ARR_LENGTH = 2;
	private static final char SIZE_DELIMITER = 'x';

	private static final char
	KEY_DELIMITER = '.', VALUE_DELIMITER = '|', ESCAPE = '\\';

	private static final String
	AD_PREFIX = "ad" + KEY_DELIMITER, BASE64_IND = "data:image", COPY_IND = "copy:";

	public static AdMap parse(SimpleConfiguration config) {
		if(config == null)
			throw new NullPointerException();

		AdMap map = new AdMap();

		for(String key : config.getKeys()) {
			if(!key.startsWith(AD_PREFIX))
				continue;

			try {
				Ad ad = parseAd(key, config.get(key));
				map.add(ad);
			} catch(RuntimeException rE) {
				log("Cannot parse key", key, rE);
			}
		}

		return map;
	}

	private static Ad parseAd(String key, String value) throws RuntimeException {
		// Parsing name
		String name = seek(key, AD_PREFIX.length(), KEY_DELIMITER);

		if(name.isEmpty())
			throw new IllegalArgumentException("name is empty");

		int caret = 0;

		// Parsing chance
		String tempChance = seek(value, caret);
		int chance = 0;

		try {
			chance = Integer.parseInt(tempChance);
			caret += tempChance.length();
		} catch(RuntimeException rE) {
			chance = 100;
			--caret;
		}


		// Parsing content
		String content = seek(value, ++caret);
		caret += content.length();

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

		return new Ad(name, chance, content, size, image);
	}

	private static String parseImage(String image) {
		if(image == null)
			return null;

		if(image.startsWith(BASE64_IND) || image.startsWith(COPY_IND))
			return image;

		URL url = ImageCache.getRes(image);
		return url == null? null : url.toString();
	}

	public static class AdMap {
		private final Map<String, AdList> map = new HashMap<String, AdList>(), unmodifiable = Collections.unmodifiableMap(map);

		public final Map<String, AdList> getMap() {
			return unmodifiable;
		}

		protected final Map<String, AdList> map() {
			return map;
		}

		public final AdList getByName(String name) {
			return map.get(name);
		}

		protected void add(AdList list) {
			if(list == null)
				throw new NullPointerException("list");

			map.put(list.name, list);
		}

		protected void add(Ad ad) {
			if(ad == null)
				throw new NullPointerException("ad");

			String name = ad.getName();
			AdList list = map.get(name);
			boolean add = list == null;

			if(add)
				list = new AdList(name);

			list.add(ad);

			if(add)
				add(list);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + map.values();
		}
	}

	public static class AdList {
		private final String name;

		private final List<Ad> list = new ArrayList<Ad>(), unmodifiable = Collections.unmodifiableList(list);

		private final Ad[] chances = new Ad[100];
		private int totalChance = 0;

		public AdList(String name) {
			if(name == null)
				throw new NullPointerException("name");

			if(name.isEmpty())
				throw new IllegalArgumentException("name is empty");

			this.name = name;
		}

		public final String getName() {
			return name;
		}

		public final List<Ad> getAds() {
			return unmodifiable;
		}

		protected final List<Ad> list() {
			return list;
		}

		public final Ad getRandom() {
			int random = new Random().nextInt(100);

			log("Chosen randomly:", random);
			return chances[random];
		}

		protected void add(Ad ad) {
			if(ad == null)
				throw new NullPointerException();

			if(totalChance + ad.chance > 100)
				throw new IllegalArgumentException("chance overflow: "+ (totalChance + ad.chance));

			list.add(ad);

			Arrays.fill(chances, totalChance, totalChance + ad.chance, ad);
			totalChance += ad.chance;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + list();
		}
	}

	public static class Ad {
		private final String name;
		private final int chance;

		private final String content;
		private final int[] size;

		private String image;

		private Ad(String name, int chance, String content, int[] size, String image) {
			if(name == null)
				throw new NullPointerException("locale");

			if(name.isEmpty())
				throw new IllegalArgumentException("locale is empty");

			if(chance < 0 || chance > 100)
				throw new IllegalArgumentException("illegal chance: "+ chance);

			if(content == null)
				throw new NullPointerException("content");

			if(content.isEmpty())
				throw new IllegalArgumentException("content is empty");

			if(size == null)
				throw new NullPointerException("size");

			if(size.length != SIZE_ARR_LENGTH)
				throw new IllegalArgumentException("size array has illegal length:"+ size.length +" (instead of "+ SIZE_ARR_LENGTH +")");

			this.name = name;
			this.chance = chance;

			this.content = content;
			this.size = size;

			this.image = image;
		}

		public final String getName() {
			return name;
		}

		public final int getChance() {
			return chance;
		}

		public final String getContent() {
			return content;
		}

		public final String getImage() {
			return image;
		}

		protected void setImage(String image) {
			this.image = image;
		}

		public final int getWidth() {
			return size[0];
		}

		public final int getHeight() {
			return size[1];
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			builder
			.append(getClass().getSimpleName())
			.append("{")
			.append("name=").append(name).append(';')
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

	private static void log(Object... o) { U.log("[AdParser]", o); }
}