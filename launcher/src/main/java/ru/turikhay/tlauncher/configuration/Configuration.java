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
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.tlauncher.ui.FlatLaf;
import ru.turikhay.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.*;

public class Configuration extends SimpleConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    private ConfigurationDefaults defaults;
    private ArgumentParser.ParsedConfigEntryMap configFromArgs;
    private boolean firstRun;
    private final boolean externalLocation;

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
        if (doesntExist) {
            config.firstRun = true;
        } else {
            if (config.getBoolean("firstRun")) {
                config.firstRun = true;
                config.set("firstRun", null);
            }
        }
        return config;
    }

    private void init(OptionSet set) {
        comments = " Legacy Launcher " + TLauncher.getBrand() + " properties\n Created in " + TLauncher.getVersion();
        defaults = ConfigurationDefaults.getInstance();
        configFromArgs = ArgumentParser.extractConfigEntries(set);

        if (getDouble("settings.version") != ConfigurationDefaults.getVersion()) {
            LOGGER.warn("Configuration is being wiped due to version incapability");
            set("settings.version", ConfigurationDefaults.getVersion(), false);
            clear();
        }

        LOGGER.debug("Config entries from args: {}", configFromArgs);

        configFromArgs.entries().forEach(c -> setForcefully(c.getPath(), c.getValue(), false));

        if (externalLocation && !Objects.equals(TLauncher.getInstance().getPackageMode().orElse(null), "flatpak")) {
            LOGGER.debug("Using configuration from an external location");

            File defFile = getDefaultFile();
            SimpleConfiguration backConfig = new SimpleConfiguration(defFile);

            if (defFile.isFile()) {
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
            if (configFromArgs.isConstant(defEntry.getKey())) {
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


        if (!LangConfiguration.getAvailableLocales().contains(locale)) {
            LOGGER.debug("We don't have localization for {}", locale);

            if (isLikelyRussianSpeakingLocale(locale.toString()) && LangConfiguration.getAvailableLocales().contains(LangConfiguration.ru_RU)) {
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

        String separateDirsValue = get("minecraft.gamedir.separate");
        if ("false".equals(separateDirsValue) || "true".equals(separateDirsValue)) {
            setSeparateDirs(getBoolean("minecraft.gamedir.separate") ? SeparateDirs.FAMILY : SeparateDirs.NONE);
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
        return !configFromArgs.isConstant(key);
    }

    public Locale getLocale() {
        return U.getLocale(get("locale"));
    }

    public boolean isLikelyRussianSpeakingLocale() {
        return isLikelyRussianSpeakingLocale(getLocale().toString());
    }

    public Configuration.ActionOnLaunch getActionOnLaunch() {
        return Configuration.ActionOnLaunch.find(get("minecraft.onlaunch")).orElse(Configuration.ActionOnLaunch.getDefault());
    }

    public LoggerType getLoggerType() {
        return LoggerType.get(get("gui.logger"));
    }

    public void setLoggerType(LoggerType loggerType) {
        //return LoggerType.get(get("gui.logger"));
        set("gui.logger", loggerType.toString());
    }

    public SeparateDirs getSeparateDirs() {
        return SeparateDirs.get(get("minecraft.gamedir.separate"));
    }

    public void setSeparateDirs(SeparateDirs separateDirs) {
        set("minecraft.gamedir.separate", separateDirs.toString().toLowerCase(Locale.ROOT));
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
            } catch (Exception ignored) {
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
            } catch (Exception ignored) {
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

        for (ReleaseType type : ReleaseType.getDefinable()) {
            boolean include = getBoolean("minecraft.versions." + type);
            if (!include) {
                filter.exclude(type);
            }
        }

        for (ReleaseType.SubType var7 : ReleaseType.SubType.values()) {
            boolean include1 = getBoolean("minecraft.versions.sub." + var7);
            if (!include1) {
                filter.exclude(var7);
            }
        }

        return filter;
    }

    public Direction getDirection(String key) {
        return Direction.parse(get(key));
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
        } catch (RuntimeException rE) {
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
        if (!configFromArgs.isConstant(key)) {
            super.set(key, value, flush);
        }
    }

    public void setForcefully(String key, Object value, boolean flush) {
        super.set(key, value, flush);
    }

    public void setForcefully(String key, Object value) {
        setForcefully(key, value, true);
    }

    @Override
    protected Properties processSavingProperties(Properties og) {
        Properties temp = copyProperties(properties);
        for (String constant : configFromArgs.constants()) {
            temp.remove(constant);
        }
        return temp;
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

    public Optional<FlatLafConfiguration> getFlatLafConfiguration() {
        return FlatLaf.parseFromMap(this);
    }

    public void setFlatLafConfiguration(Object configuration) {
        if (!(configuration instanceof FlatLafConfiguration)) {
            throw new IllegalArgumentException();
        }
        putAll(((FlatLafConfiguration) configuration).toMap());
    }

    public File getFile() {
        return !isSaveable() ? null : (File) input;
    }

    public static boolean isLikelyRussianSpeakingLocale(String l) {
        return "ru_RU".equals(l) || "uk_UA".equals(l) || "be_BY".equals(l);
    }

    private static File getDefaultFile() {
        return MinecraftUtil.getSystemRelatedDirectory(TLauncher.getSettingsFile());
    }

    public enum ActionOnLaunch {
        HIDE,
        EXIT,
        NOTHING;

        @Nonnull
        public static Optional<ActionOnLaunch> find(@Nullable String name) {
            if (name == null) return Optional.empty();
            return Arrays.stream(values()).filter(it -> it.toString().equalsIgnoreCase(name)).findAny();
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
            }

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

    public enum SeparateDirs {
        FAMILY,
        VERSION,
        NONE;

        public static boolean parse(String val) {
            if (val != null) {
                SeparateDirs[] var4;
                int var3 = (var4 = values()).length;

                for (int var2 = 0; var2 < var3; ++var2) {
                    SeparateDirs cur = var4[var2];
                    if (cur.toString().equalsIgnoreCase(val)) {
                        return true;
                    }
                }

                return val.equals("true") || val.equals("false");
            }
            return false;
        }

        public static SeparateDirs get(String val) {
            SeparateDirs[] var4;
            int var3 = (var4 = values()).length;
            for (int var2 = 0; var2 < var3; ++var2) {
                SeparateDirs cur = var4[var2];
                if (cur.toString().equalsIgnoreCase(val)) {
                    return cur;
                }
            }
            return null;
        }
    }
}
