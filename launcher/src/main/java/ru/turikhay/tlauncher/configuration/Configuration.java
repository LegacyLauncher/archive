package ru.turikhay.tlauncher.configuration;

import com.github.zafarkhaja.semver.Version;
import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;
import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class Configuration extends SimpleConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Lazy<List<Locale>> DEFAULT_LOCALES = Lazy.of(Configuration::getDefaultLocales);

    private ConfigurationDefaults defaults;
    private Map<String, Object> constants;
    private boolean firstRun, externalLocation;

    private Configuration(URL url, OptionSet set) throws IOException {
        super(url);
        externalLocation = true;
        init(set);
    }

    private Configuration(File file, OptionSet set) {
        super(file);
        externalLocation = !file.equals(getDefaultFile());
        init(set);
    }

    public static Configuration createConfiguration(OptionSet set) throws IOException {
        Object path = set != null ? set.valueOf("settings") : null;
        File file;

        if (path == null) {
            file = FileUtil.getNeighborFile("tlauncher.cfg");
            if (!file.isFile()) {
                file = FileUtil.getNeighborFile("tlauncher.properties");
            }
            if (!file.isFile()) {
                file = getDefaultFile();
            }
        } else {
            LOGGER.debug("--settings argument: {}", path);
            file = new File(path.toString());
        }

        boolean doesntExist = !file.isFile();
        if (doesntExist) {
            LOGGER.debug("Creating file: {}", file);
            FileUtil.createFile(file);
        }

        LOGGER.info("Reading configuration from: {}", file);

        Configuration config = new Configuration(file, set);
        config.firstRun = doesntExist;
        return config;
    }

    private void init(OptionSet set) {
        comments = " TLauncher " + TLauncher.getBrand() + " properties\n Created in " + TLauncher.getVersion() + (TLauncher.isBeta() ? " BETA" : "");
        defaults = ConfigurationDefaults.getInstance();
        constants = ArgumentParser.parse(set);

        if (getDouble("settings.version") != ConfigurationDefaults.getVersion()) {
            LOGGER.warn("Configuration is being wiped due to version incapability");
            set("settings.version", ConfigurationDefaults.getVersion(), false);
            clear();
        }

        LOGGER.debug("Constants: {}", constants);
        set(constants, false);

        if(externalLocation) {
            LOGGER.debug("Using configuration from an external location");

            File defFile = getDefaultFile();
            SimpleConfiguration backConfig = new SimpleConfiguration(defFile);

            if(defFile.isFile()) {
                //log("Default file exists, backing up some values...");
            } else {
                LOGGER.debug("Default file doesn't exist, oops...");
                backConfig.set("settings.version", ConfigurationDefaults.getVersion());
                backConfig.set("client", UUID.randomUUID());
                backConfig.store();
            }

            set("client", backConfig.get("client"), false);
        }

        try {
            UUID.fromString(get("client"));
        } catch (RuntimeException rE) {
            LOGGER.debug("Recreating UUID...");
            set("client", UUID.randomUUID(), false);
        }

        LOGGER.info("UUID: {}", getClient());

        for (Entry<String, Object> defEntry : defaults.getMap().entrySet()) {
            if (constants.containsKey(defEntry.getKey())) {
                continue;
            }

            String value = get(defEntry.getKey());
            try {
                PlainParser.parse(get(defEntry.getKey()), defEntry.getValue());
            } catch (RuntimeException rE) {
                LOGGER.warn("Could not parse {}, got: {}", defEntry.getKey(), value);
                set(defEntry.getKey(), defEntry.getValue(), false);
            }
        }

        Locale locale = U.getLocale(get("locale"));
        if (locale == null) {
            LOGGER.warn("Locale is not supported by Java: {}", get("locale"));
            LOGGER.warn("May be system default?");
            locale = Locale.getDefault();
        }

        if (!DEFAULT_LOCALES.get().contains(locale)) {
            LOGGER.debug("We don't have localization for {}", locale);

            if (isUSSRLocale(locale.toString()) && DEFAULT_LOCALES.get().contains(LangConfiguration.ru_RU)) {
                locale = LangConfiguration.ru_RU;
            } else {
                locale = Locale.US;
            }

            LOGGER.debug("Selecting {}", locale);
        }
        set("locale", locale);

        int oldFontSize = getInteger("gui.font.old");
        if (oldFontSize == 0) {
            set("gui.font.old", getInteger("gui.font"));
        }

        LOGGER.debug("Using configuration: {}", properties);

        if (isSaveable()) {
            try {
                save();
            } catch (IOException ioE) {
                LOGGER.warn("Couldn't save config", ioE);
            }
        }
    }

    public boolean isCertFixed() {
        return !getBoolean("connection.ssl");
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public boolean isSaveable(String key) {
        return !constants.containsKey(key);
    }

    public Locale getLocale() {
        return U.getLocale(get("locale"));
    }

    public boolean isUSSRLocale() {
        return isUSSRLocale(getLocale().toString());
    }

    public Locale[] getLocales() {
        return DEFAULT_LOCALES.get().toArray(new Locale[0]);
    }

    public Configuration.ActionOnLaunch getActionOnLaunch() {
        return Configuration.ActionOnLaunch.get(get("minecraft.onlaunch"));
    }

    public LoggerType getLoggerType() {
        return LoggerType.get(get("gui.logger"));
    }

    public void setLoggerType(LoggerType loggerType) {
        //return LoggerType.get(get("gui.logger"));
        set("gui.logger", loggerType.toString());
    }

    public int[] getClientWindowSize() {
        String plainValue = get("minecraft.size");
        int[] value = new int[2];
        if (plainValue == null) {
            return new int[2];
        } else {
            try {
                IntegerArray arr = IntegerArray.parseIntegerArray(plainValue);
                value[0] = arr.get(0);
                value[1] = arr.get(1);
            } catch (Exception var4) {
            }

            return value;
        }
    }

    public int[] getLauncherWindowSize() {
        String plainValue = get("gui.size");
        int[] value = new int[2];
        if (plainValue == null) {
            return new int[2];
        } else {
            try {
                IntegerArray arr = IntegerArray.parseIntegerArray(plainValue);
                value[0] = arr.get(0);
                value[1] = arr.get(1);
            } catch (Exception var4) {
            }
            return value;
        }
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
        Iterator var3 = ReleaseType.getDefinable().iterator();

        while (var3.hasNext()) {
            ReleaseType type = (ReleaseType) var3.next();
            boolean include = getBoolean("minecraft.versions." + type);
            if (!include) {
                filter.exclude(type);
            }
        }

        ReleaseType.SubType[] var5;
        int var9 = (var5 = ReleaseType.SubType.values()).length;

        for (int var8 = 0; var8 < var9; ++var8) {
            ReleaseType.SubType var7 = var5[var8];
            boolean include1 = getBoolean("minecraft.versions.sub." + var7);
            if (!include1) {
                filter.exclude(var7);
            }
        }

        return filter;
    }

    public Direction getDirection(String key) {
        return Reflect.parseEnum(Direction.class, get(key));
    }

    public Proxy getProxy() {
        return Proxy.NO_PROXY;
    }

    public UUID getClient() {
        try {
            return UUID.fromString(get("client"));
        } catch (Exception var2) {
            return refreshClient();
        }
    }

    public UUID refreshClient() {
        UUID newId = UUID.randomUUID();
        set("client", newId);
        return newId;
    }

    private final Version zeroVersion = Version.forIntegers(0, 0, 0);
    public Version getVersion(String path) {
        try {
            return Version.valueOf(get(path));
        } catch(RuntimeException rE) {
            return zeroVersion;
        }
    }

    public boolean isUsingSystemLookAndFeel() {
        return getBoolean("gui.systemlookandfeel");
    }

    public void setUsingSystemLookAndFeel(boolean use) {
        set("gui.systemlookandfeel", use, false);
    }

    public float getFontSize() {
        return getFloat("gui.font");
    }

    public String getDefault(String key) {
        return getStringOf(defaults.get(key));
    }

    public int getDefaultInteger(String key) {
        return getIntegerOf(defaults.get(key), 0);
    }

    public double getDefaultDouble(String key) {
        return getDoubleOf(defaults.get(key), 0.0D);
    }

    public float getDefaultFloat(String key) {
        return getFloatOf(defaults.get(key), 0.0F);
    }

    public long getDefaultLong(String key) {
        return getLongOf(defaults.get(key), 0L);
    }

    public boolean getDefaultBoolean(String key) {
        return getBooleanOf(defaults.get(key), false);
    }

    public void set(String key, Object value, boolean flush) {
        if (!constants.containsKey(key)) {
            super.set(key, value, flush);
        }
    }

    public void setForcefully(String key, Object value, boolean flush) {
        super.set(key, value, flush);
    }

    public void setForcefully(String key, Object value) {
        setForcefully(key, value, true);
    }

    public void save() throws IOException {
        if (!isSaveable()) {
            throw new UnsupportedOperationException();
        } else {
            Properties temp = copyProperties(properties);
            Iterator var3 = constants.keySet().iterator();

            while (var3.hasNext()) {
                String file = (String) var3.next();
                temp.remove(file);
            }

            File file1 = (File) input;
            temp.store(new FileOutputStream(file1), comments);
        }
    }

    public <C extends Configurable> C get(Class<C> configurable) {
        C c;
        try {
            c = configurable.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        c.load(this);
        return c;
    }

    public <C extends Configurable> void set(C configurable) {
        configurable.save(this);
    }

    public File getFile() {
        return !isSaveable() ? null : (File) input;
    }

    private static List<Locale> getDefaultLocales() {
        ArrayList<Locale> l = new ArrayList<>();
        String[] ll = Static.getLangList();
        for (String locale : ll) {
            Locale loc = U.getLocale(locale);
            if (loc == null) {
                LOGGER.warn("Default locale is unavailable: {}", locale);
                Sentry.capture(new EventBuilder()
                        .withLevel(Event.Level.WARNING)
                        .withMessage("default locale is unavailable: " + locale)
                );
            }
            l.add(loc);
        }
        return l;
    }


    public static boolean isUSSRLocale(String l) {
        return "ru_RU".equals(l) || "uk_UA".equals(l) || "be_BY".equals(l);
    }

    private static File getDefaultFile() {
        return MinecraftUtil.getSystemRelatedDirectory(TLauncher.getSettingsFile());
    }

    public enum ActionOnLaunch {
        HIDE,
        EXIT,
        NOTHING;

        public static boolean parse(String val) {
            if (val == null) {
                return false;
            } else {
                Configuration.ActionOnLaunch[] var4;
                int var3 = (var4 = values()).length;

                for (int var2 = 0; var2 < var3; ++var2) {
                    Configuration.ActionOnLaunch cur = var4[var2];
                    if (cur.toString().equalsIgnoreCase(val)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public static Configuration.ActionOnLaunch get(String val) {
            Configuration.ActionOnLaunch[] var4;
            int var3 = (var4 = values()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                Configuration.ActionOnLaunch cur = var4[var2];
                if (cur.toString().equalsIgnoreCase(val)) {
                    return cur;
                }
            }

            return null;
        }

        public String toString() {
            return super.toString().toLowerCase(java.util.Locale.ROOT);
        }

        public static Configuration.ActionOnLaunch getDefault() {
            return HIDE;
        }
    }

    public enum LoggerType {
        GLOBAL,
        NONE;

        public static boolean parse(String val) {
            if (val == null) {
                return false;
            } else {
                LoggerType[] var4;
                int var3 = (var4 = values()).length;

                for (int var2 = 0; var2 < var3; ++var2) {
                    LoggerType cur = var4[var2];
                    if (cur.toString().equalsIgnoreCase(val)) {
                        return true;
                    }
                }

                return false;
            }
        }

        public static LoggerType get(String val) {
            LoggerType[] var4;
            int var3 = (var4 = values()).length;

            for (int var2 = 0; var2 < var3; ++var2) {
                LoggerType cur = var4[var2];
                if (cur.toString().equalsIgnoreCase(val)) {
                    return cur;
                }
            }

            return null;
        }

        public String toString() {
            return super.toString().toLowerCase(java.util.Locale.ROOT);
        }

        public static LoggerType getDefault() {
            return NONE;
        }
    }
}
