package net.legacylauncher.bootstrap.ui.flatlaf;

import com.formdev.flatlaf.*;
import net.legacylauncher.bootstrap.ui.UserInterface;
import net.legacylauncher.bootstrap.ui.flatlaf.themedetector.ThemeDetector;
import net.legacylauncher.util.shared.FlatLafConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class FlatLaf {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlatLaf.class);

    public static void initialize(FlatLafConfiguration config) {
        if(config.isEnabled()) {
            FlatLafConfiguration.Theme theme = config.getSelected().orElse(detectTheme());
            setUIProperties(config.getUiPropertiesFiles().get(theme));
            setLaf(theme, config.getThemeFiles().get(theme));
        } else {
            LOGGER.info("FlatLaf is not enabled. Skipping initialization");
        }
    }

    private static FlatLafConfiguration.Theme detectTheme() {
        LOGGER.info("Detecting system theme");
        return ThemeDetector.detectTheme();
    }

    private static void setLaf(FlatLafConfiguration.Theme theme, String themeFile) {
        com.formdev.flatlaf.FlatLaf laf = null;
        boolean useSystemTheme = false;
        if (themeFile != null) {
            if(themeFile.startsWith(":")) {
                switch (themeFile.substring(1)) {
                    case "darcula":
                        laf = new FlatDarculaLaf();
                        break;
                    case "dark":
                        laf = new FlatDarkLaf();
                        break;
                    case "intellij":
                        laf = new FlatIntelliJLaf();
                        break;
                    case "light":
                        laf = new FlatLightLaf();
                        break;
                    case "system":
                        useSystemTheme = true;
                        break;
                    default:
                        LOGGER.warn("Unknown theme id: {}", themeFile);
                        laf = new FlatLightLaf();
                        break;
                }
            } else {
                laf = loadLafFromThemeFile(themeFile);
            }
        }
        if (useSystemTheme) {
            LOGGER.info("System L&F is selected for theme {}", theme);
            if (theme == FlatLafConfiguration.Theme.DARK) {
                UIManager.put("laf.dark", true);
            }
            UserInterface.setSystemLookAndFeel();
            return;
        }
        if(laf == null) {
            switch (theme) {
                case DARK:
                    laf = new FlatDarkLaf();
                    break;
                case LIGHT:
                    laf = new FlatLightLaf();
                    break;
                default:
                    throw new IllegalArgumentException(theme.name());
            }
        }
        setLaf(laf);
    }

    private static com.formdev.flatlaf.FlatLaf loadLafFromThemeFile(String themeFile) {
        LOGGER.info("Loading L&F theme from {}", themeFile);
        try(FileInputStream in = new FileInputStream(themeFile)) {
            return IntelliJTheme.createLaf(in);
        } catch (IOException e) {
            LOGGER.error("Couldn't load IntelliJ theme from file: {}", themeFile, e);
            return null;
        }
    }

    private static void setLaf(com.formdev.flatlaf.FlatLaf lookAndFeel) {
        LOGGER.info("Setting L&F: {}", lookAndFeel);
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            LOGGER.error("Couldn't set L&F", e);
        }
    }

    private static void setUIProperties(String uiPropertiesFile) {
        if(uiPropertiesFile == null) {
            LOGGER.info("No UI properties file, skipping");
            return;
        }
        LOGGER.info("Setting addon properties file: {}", uiPropertiesFile);
        Addon.PROPERTIES_FILE = uiPropertiesFile;
    }
}
