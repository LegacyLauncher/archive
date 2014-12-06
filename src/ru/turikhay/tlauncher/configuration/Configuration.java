package ru.turikhay.tlauncher.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher.ConsoleVisibility;
import ru.turikhay.util.Direction;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Configuration extends SimpleConfiguration {

	private ConfigurationDefaults defaults;
	private Map<String, Object> constants; // unsaveable keys

	private List<Locale> defaultLocales;
	private List<Locale> supportedLocales;

	private boolean firstRun;

	private Configuration(URL url, OptionSet set) throws IOException {
		super(url);
		init(set);
	}

	private Configuration(File file, OptionSet set) {
		super(file);
		init(set);
	}

	public static Configuration createConfiguration(OptionSet set) throws IOException {
		Object path = (set != null) ? set.valueOf("settings") : null;

		File file;

		if (path == null) {

			file = FileUtil.getNeighborFile("tlauncher.cfg");

			if (!file.isFile())
				file = FileUtil.getNeighborFile("tlauncher.properties");

			if(!file.isFile())
				file = MinecraftUtil.getSystemRelatedDirectory(TLauncher.getSettingsFile());

		} else {
			file = new File(path.toString());
		}

		boolean doesntExist = !file.isFile();

		if(doesntExist) {
			U.log("Creating configuration file...");
			FileUtil.createFile(file);
		}

		U.log("Loading configuration from file:", file);

		Configuration config = new Configuration(file, set);
		config.firstRun = doesntExist;

		return config;
	}

	public static Configuration createConfiguration() throws IOException {
		return createConfiguration(null);
	}

	private void init(OptionSet set) {
		// Initializing default values
		this.comments = " TLauncher configuration file" + '\n' + " Created in "
				+ TLauncher.getBrand() + " " + TLauncher.getVersion();

		this.defaults = new ConfigurationDefaults();
		this.constants = ArgumentParser.parse(set);
		set(constants, false);

		log("Constant values:", constants);

		int version = ConfigurationDefaults.getVersion();

		if (getDouble("settings.version") != version)
			this.clear(); // Clean up old values

		set("settings.version", version, false);

		// Parsing values
		for (Entry<String, Object> curen : defaults.getMap().entrySet()) {
			String key = curen.getKey();

			if (constants.containsKey(key)) {
				log("Key \"" + key + "\" is unsaveable!");
				continue;
			}

			String value = get(key);
			Object defvalue = curen.getValue();

			if (defvalue != null)
				try {
					PlainParser.parse(value, defvalue);
				} catch (ParseException e) {
					log("Key \"" + key + "\" is invalid!", e);
					set(key, defvalue, false);
				}
		}

		// Locale initializing
		this.defaultLocales = getDefaultLocales();
		this.supportedLocales = getSupportedLocales();
		Locale selected = getLocaleOf(get("locale"));

		if (selected == null) { // Selected locale is not supported
			log("Selected locale is not supported, rolling back to default one");
			selected = Locale.getDefault();
		}

		if (!supportedLocales.contains(selected)) { // Default locale is not
			// supported.
			log("Default locale is not supported, rolling back to global default one");
			selected = Locale.US;
		}

		set("locale", selected, false);

		if (isSaveable())
			try {
				save();
			} catch (IOException e) {
				log("Cannot save value!", e);
			}
	}

	public boolean isFirstRun() {
		return firstRun;
	}

	public boolean isSaveable(String key) {
		return !constants.containsKey(key);
	}

	public Locale getLocale() {
		return getLocaleOf(get("locale"));
	}

	public Locale[] getLocales() {
		Locale[] locales = new Locale[supportedLocales.size()];
		return supportedLocales.toArray(locales);
	}

	public ActionOnLaunch getActionOnLaunch() {
		return ActionOnLaunch.get(get("minecraft.onlaunch"));
	}

	public ConsoleType getConsoleType() {
		return ConsoleType.get(get("gui.console"));
	}

	public ConnectionQuality getConnectionQuality() {
		return ConnectionQuality.get(get("connection"));
	}

	public int[] getClientWindowSize() {
		String plainValue = get("minecraft.size");
		int[] value = new int[2];

		if (plainValue == null)
			return new int[2];

		try {
			IntegerArray arr = IntegerArray.parseIntegerArray(plainValue);
			value[0] = arr.get(0);
			value[1] = arr.get(1);
		} catch (Exception ignored) {
		}

		return value;
	}

	public int[] getLauncherWindowSize() {
		String plainValue = get("gui.size");
		int[] value = new int[2];

		if (plainValue == null)
			return new int[2];

		try {
			IntegerArray arr = IntegerArray.parseIntegerArray(plainValue);
			value[0] = arr.get(0);
			value[1] = arr.get(1);
		} catch (Exception ignored) {
		}

		return value;
	}

	public int[] getDefaultClientWindowSize() {
		String plainValue = getDefault("minecraft.size");
		return IntegerArray.parseIntegerArray(plainValue).toArray();
	}

	public int[] getDefaultLauncherWindowSize() {
		String plainValue = getDefault("gui.size");
		return IntegerArray.parseIntegerArray(plainValue).toArray();
	}

	public VersionFilter getVersionFilter() {
		VersionFilter filter = new VersionFilter();

		for (ReleaseType type : ReleaseType.getDefinable()) {
			if (type.equals(ReleaseType.UNKNOWN))
				continue; // Unknown versions are always included

			boolean include = getBoolean("minecraft.versions." + type);
			if (!include)
				filter.excludeType(type);
		}

		return filter;
	}

	public Direction getDirection(String key) {
		return Reflect.parseEnum(Direction.class, get(key));
	}

	@Override
	public String getDefault(String key) {
		return getStringOf(defaults.get(key));
	}

	@Override
	public int getDefaultInteger(String key) {
		return getIntegerOf(defaults.get(key), 0);
	}

	@Override
	public double getDefaultDouble(String key) {
		return getDoubleOf(defaults.get(key), 0);
	}

	@Override
	public float getDefaultFloat(String key) {
		return getFloatOf(defaults.get(key), 0);
	}

	@Override
	public long getDefaultLong(String key) {
		return getLongOf(defaults.get(key), 0);
	}

	@Override
	public boolean getDefaultBoolean(String key) {
		return getBooleanOf(defaults.get(key), false);
	}

	@Override
	public void set(String key, Object value, boolean flush) {
		if (constants.containsKey(key))
			return;

		super.set(key, value, flush);
	}

	public void setForcefully(String key, Object value, boolean flush) {
		super.set(key, value, flush);
	}

	public void setForcefully(String key, Object value) {
		setForcefully(key, value, true);
	}

	@Override
	public void save() throws IOException {
		if (!isSaveable())
			throw new UnsupportedOperationException();

		Properties temp = copyProperties(properties);
		for (String key : constants.keySet())
			temp.remove(key);

		File file = (File) input;
		temp.store(new FileOutputStream(file), comments);
	}

	public File getFile() {
		if (!isSaveable())
			return null;
		return (File) input;
	}

	private List<Locale> getSupportedLocales() {
		log("Searching for supported locales...");

		Pattern lang_pattern = Pattern.compile("lang/([\\w]+)\\.ini");
		File file = FileUtil.getRunningJar();
		List<Locale> locales = new ArrayList<Locale>();

		try {
			URL jar = file.toURI().toURL();

			ZipInputStream zip = new ZipInputStream(jar.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null)
					break;

				String name = e.getName();
				Matcher mt = lang_pattern.matcher(name);
				if (!mt.matches())
					continue;

				log("Found locale:", mt.group(1));
				locales.add(getLocaleOf(mt.group(1)));
			}
		} catch (Exception e) {
			log("Cannot get locales!", e);
			return defaultLocales;
		}

		if (locales.isEmpty())
			return defaultLocales;

		return locales;
	}

	private static List<Locale> getDefaultLocales() {
		List<Locale> l = new ArrayList<Locale>();
		l.add(getLocaleOf("en_US"));
		l.add(getLocaleOf("ru_RU"));
		l.add(getLocaleOf("uk_UA"));
		return l;
	}

	public static Locale getLocaleOf(String locale) {
		if (locale == null)
			return null;

		for (Locale cur : Locale.getAvailableLocales())
			if (cur.toString().equals(locale))
				return cur;

		return null;
	}

	public static enum ConnectionQuality {
		GOOD(2, 5, Downloader.MAX_THREADS, 15000), NORMAL(5, 10,
				Downloader.MAX_THREADS / 2, 45000), BAD(10, 20, 1, 120000);

		private final int minTries, maxTries, maxThreads, timeout;
		private final int[] configuration;

		ConnectionQuality(int minTries, int maxTries, int maxThreads,
				int timeout) {
			this.minTries = minTries;
			this.maxTries = maxTries;
			this.maxThreads = maxThreads;

			this.timeout = timeout;

			this.configuration = new int[] { minTries, maxTries, maxThreads };
		}

		public static boolean parse(String val) {
			if (val == null)
				return false;
			for (ConnectionQuality cur : values())
				if (cur.toString().equalsIgnoreCase(val))
					return true;
			return false;
		}

		public static ConnectionQuality get(String val) {
			for (ConnectionQuality cur : values())
				if (cur.toString().equalsIgnoreCase(val))
					return cur;
			return null;
		}

		public int[] getConfiguration() {
			return configuration;
		}

		public int getMinTries() {
			return minTries;
		}

		public int getMaxTries() {
			return maxTries;
		}

		public int getMaxThreads() {
			return maxThreads;
		}

		public int getTries(boolean fast) {
			return fast ? minTries : maxTries;
		}

		public int getTimeout() {
			return timeout;
		}

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static ConnectionQuality getDefault() {
			return GOOD;
		}
	}

	public enum ConsoleType {
		GLOBAL, MINECRAFT, NONE;

		public static boolean parse(String val) {
			if (val == null)
				return false;
			for (ConsoleType cur : values())
				if (cur.toString().equalsIgnoreCase(val))
					return true;
			return false;
		}

		public static ConsoleType get(String val) {
			for (ConsoleType cur : values())
				if (cur.toString().equalsIgnoreCase(val))
					return cur;
			return null;
		}

		public ConsoleVisibility getVisibility() {
			return this == GLOBAL ? ConsoleVisibility.NONE : this == MINECRAFT ? ConsoleVisibility.ALWAYS : ConsoleVisibility.ON_CRASH;
		}

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static ConsoleType getDefault() {
			return NONE;
		}
	}

	public enum ActionOnLaunch {
		HIDE, EXIT, NOTHING;

		public static boolean parse(String val) {
			if (val == null)
				return false;
			for (ActionOnLaunch cur : values())
				if (cur.toString().equalsIgnoreCase(val))
					return true;
			return false;
		}

		public static ActionOnLaunch get(String val) {
			for (ActionOnLaunch cur : values())
				if (cur.toString().equalsIgnoreCase(val))
					return cur;
			return null;
		}

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}

		public static ActionOnLaunch getDefault() {
			return HIDE;
		}
	}
}
