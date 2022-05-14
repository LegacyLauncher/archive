package ru.turikhay.tlauncher.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.util.Lazy;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration.getVersion;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FlatLaf {
    private static final Logger LOGGER = LogManager.getLogger(FlatLaf.class);
    private static final String SUPPORTED_CONFIG_VERSION = "v1";

    private static final Lazy<Boolean> IS_SUPPORTED = Lazy.of(() -> {
        try {
            if(!TLauncher.getInstance().hasCapability("has_flatlaf")) {
                throw new RuntimeException("capability missing: has_flatlaf");
            }
            String configVersion = getVersion();
            if(!configVersion.equals(SUPPORTED_CONFIG_VERSION)) {
                throw new RuntimeException("version not supported: " + configVersion);
            }
        } catch(Throwable t) {
            LOGGER.warn("FlatLafConfiguration not available: {}", t.toString());
            return false;
        }
        return true;
    });

    private static final Lazy<List<String>> STATES = Lazy.of(() -> {
        if(!isSupported()) {
            return Collections.emptyList();
        }
        return Arrays.stream(FlatLafConfiguration.State.values())
                .map(FlatLafConfiguration.State::toString)
                .collect(Collectors.toList());
    });

    public static Map<String, String> getDefaults() {
        if(isSupported()) {
            return FlatLafConfiguration.getDefaults();
        } else {
            return Collections.emptyMap();
        }
    }

    public static Optional<FlatLafConfiguration> parseFromMap(Map<String, String> map) {
        if(isSupported()) {
            return Optional.of(FlatLafConfiguration.parseFromMap(map));
        } else {
            return Optional.empty();
        }
    }

    public static BufferedImage loadDefaultBackgroundFromThemeFile(String themeFile) {
        if (!themeFile.startsWith(":")) { // not a selector
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(themeFile), StandardCharsets.UTF_8)) {
                JsonElement json = JsonParser.parseReader(reader);
                JsonObject o = json.getAsJsonObject();
                if (o.has("_tl")) {
                    JsonObject tlSection = o.getAsJsonObject("_tl");
                    if (tlSection.has("defaultBackground")) {
                        String defaultBackgroundPath = tlSection.getAsJsonPrimitive("defaultBackground")
                                .getAsString();
                        return ImageIO.read(new File(defaultBackgroundPath));
                    }
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Couldn't read default background from theme file: {}", themeFile, e);
            }
        }
        return null;
    }

    public static Optional<FlatLafConfiguration.Theme> getSelectedNowTheme(Optional<FlatLafConfiguration> configuration) {
        if (isSupported() && configuration.isPresent() && configuration.get().isEnabled()) {
            FlatLafConfiguration flatLafConfiguration = configuration.get();
            return Optional.of(flatLafConfiguration.getSelected().orElse(
                    UIManager.getBoolean("laf.dark") ?
                            FlatLafConfiguration.Theme.DARK : FlatLafConfiguration.Theme.LIGHT)
            );
        }
        return Optional.empty();
    }

    public static boolean isSupported() {
        return IS_SUPPORTED.get();
    }

    public static List<String> getStates() {
        return STATES.get();
    }
}
