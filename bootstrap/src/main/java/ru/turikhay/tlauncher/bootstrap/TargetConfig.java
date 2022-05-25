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
import java.util.LinkedHashMap;
import java.util.Map;
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

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public boolean isFirstRun() {
        return getBoolean("firstRun");
    }

    public Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return map;
    }

    public static TargetConfig readConfigFromFile(Path file) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (IOException e) {
            U.log("[TargetConfig]", "Couldn't read", file, e);
        }
        return new TargetConfig(properties);
    }

    public static Path getDefaultConfigFilePath(String brand) {
        return OS.getDefaultFolder().resolve(brandToConfigFileName(brand) + ".properties");
    }

    private static String brandToConfigFileName(String brand) {
        return brand.startsWith("legacy") ? "legacy" : brand;
    }
}
