package ru.turikhay.tlauncher.bootstrap;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TargetConfig {
    private final Properties properties;

    public TargetConfig(Properties properties) {
        this.properties = properties;
    }

    public TargetConfig() {
        this(new Properties());
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public String getClient() {
        return get("client");
    }

    public boolean isSwitchToBeta() {
        return getBoolean("bootstrap.switchToBeta");
    }

    public static TargetConfig readConfigFromFile(Path file) throws IOException {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        }
        return new TargetConfig(properties);
    }

    public static TargetConfig readConfigUsingContext(String brand, String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        ArgumentAcceptingOptionSpec<String> settingsOption = parser.accepts("settings").withRequiredArg();
        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (OptionException e) {
            U.log("[TargetConfig][WARN]", "Couldn't parse program args", e);
            options = null;
        }
        Path configFile;
        if (options != null && options.has(settingsOption)) {
            configFile = Paths.get(options.valueOf(settingsOption));
        } else {
            configFile = OS.getDefaultFolder().resolve(brandToConfigFileName(brand) + ".properties");
        }
        return readConfigFromFile(configFile);
    }

    private static String brandToConfigFileName(String brand) {
        return brand.startsWith("legacy") ? "legacy" : brand;
    }
}
