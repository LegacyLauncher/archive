package ru.turikhay.tlauncher.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public class SimpleConfiguration implements AbstractConfiguration {
	protected final Properties properties;
	protected Object input;
	protected String comments;

	public SimpleConfiguration() {
		this.properties = new Properties();
	}

	public SimpleConfiguration(InputStream stream) throws IOException {
		this();
		loadFromStream(properties, stream);

		input = stream;
	}

	public SimpleConfiguration(File file) {
		this();

		try {
			loadFromFile(properties, file);
		} catch (Exception e) {
			log("Error loading config from file:", e);
		}

		input = file;
	}

	public SimpleConfiguration(URL url) throws IOException {
		this();
		loadFromURL(properties, url);

		input = url;
	}

	@Override
	public String get(String key) {
		return getStringOf(properties.getProperty(key));
	}

	protected String getStringOf(Object obj) {
		String s;
		if (obj == null)
			s = null;
		else {
			s = obj.toString();
			if (s.isEmpty())
				s = null;
		}
		return s;
	}

	public void set(String key, Object value, boolean flush) {
		if (key == null)
			throw new NullPointerException();

		if (value == null)
			properties.remove(key);
		else
			properties.setProperty(key, value.toString());

		if (flush && isSaveable())
			store();
	}

	@Override
	public void set(String key, Object value) {
		set(key, value, true);
	}

	public void set(Map<String, Object> map, boolean flush) {
		for (Entry<String, Object> en : map.entrySet()) {
			String key = en.getKey();
			Object value = en.getValue();

			if (value == null)
				properties.remove(key);
			else
				properties.setProperty(key, value.toString());
		}

		if (flush && isSaveable())
			store();
	}

	public void set(Map<String, Object> map) {
		set(map, false);
	}

	public Set<String> getKeys() {
		Set<String> set = new HashSet<String>();

		for (Object obj : properties.keySet())
			set.add(getStringOf(obj));

		return Collections.unmodifiableSet(set);
	}

	@Override
	public String getDefault(String key) {
		return null;
	}

	public int getInteger(String key, int def) {
		return getIntegerOf(get(key), 0);
	}

	@Override
	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	protected int getIntegerOf(Object obj, int def) {
		try {
			return Integer.parseInt(obj.toString());
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public double getDouble(String key) {
		return getDoubleOf(get(key), 0);
	}

	protected double getDoubleOf(Object obj, double def) {
		try {
			return Double.parseDouble(obj.toString());
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public float getFloat(String key) {
		return getFloatOf(get(key), 0);
	}

	protected float getFloatOf(Object obj, float def) {
		try {
			return Float.parseFloat(obj.toString());
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public long getLong(String key) {
		return getLongOf(get(key), 0);
	}

	protected long getLongOf(Object obj, long def) {
		try {
			return Long.parseLong(obj.toString());
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public boolean getBoolean(String key) {
		return getBooleanOf(get(key), false);
	}

	protected boolean getBooleanOf(Object obj, boolean def) {
		try {
			return StringUtil.parseBoolean(obj.toString());
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public int getDefaultInteger(String key) {
		return 0;
	}

	@Override
	public double getDefaultDouble(String key) {
		return 0;
	}

	@Override
	public float getDefaultFloat(String key) {
		return 0;
	}

	@Override
	public long getDefaultLong(String key) {
		return 0;
	}

	@Override
	public boolean getDefaultBoolean(String key) {
		return false;
	}

	@Override
	public void save() throws IOException {
		if (!isSaveable())
			throw new UnsupportedOperationException();

		File file = (File) input;
		properties.store(new FileOutputStream(file), comments);
	}

	public void store() {
		try {
			save();
		} catch (IOException e) {
			log("Cannot store values!", e);
		}
	}

	@Override
	public void clear() {
		properties.clear();
	}

	public boolean isSaveable() {
		return input != null && input instanceof File;
	}

	private static void loadFromStream(Properties properties, InputStream stream)
			throws IOException {
		if (stream == null)
			throw new NullPointerException();

		Reader reader = new InputStreamReader(new BufferedInputStream(stream),
				Charset.forName("UTF-8"));
		properties.clear();
		properties.load(reader);
	}

	static Properties loadFromStream(InputStream stream) throws IOException {
		Properties properties = new Properties();
		loadFromStream(properties, stream);

		return properties;
	}

	private static void loadFromFile(Properties properties, File file)
			throws IOException {
		if (file == null)
			throw new NullPointerException();

		FileInputStream stream = new FileInputStream(file);
		loadFromStream(properties, stream);
	}

	protected static Properties loadFromFile(File file) throws IOException {
		Properties properties = new Properties();
		loadFromFile(properties, file);

		return properties;
	}

	private static void loadFromURL(Properties properties, URL url)
			throws IOException {
		if (url == null)
			throw new NullPointerException();

		InputStream connection = url.openStream();
		loadFromStream(properties, connection);
	}

	protected static Properties loadFromURL(URL url) throws IOException {
		Properties properties = new Properties();
		loadFromURL(properties, url);

		return properties;
	}

	protected static void copyProperties(Properties src, Properties dest,
			boolean wipe) {
		if (src == null)
			throw new NullPointerException("src is NULL");

		if (dest == null)
			throw new NullPointerException("dest is NULL");

		if (wipe)
			dest.clear();

		for (Entry<Object, Object> en : src.entrySet()) {
			String key = en.getKey() == null ? null : en.getKey().toString(), value = en
					.getKey() == null ? null : en.getValue().toString();
			dest.setProperty(key, value);
		}
	}

	protected static Properties copyProperties(Properties src) {
		Properties properties = new Properties();
		copyProperties(src, properties, false);

		return properties;
	}

	void log(Object... o) {
		U.log("[" + getClass().getSimpleName() + "]", o);
	}
}
