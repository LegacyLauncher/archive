package net.legacylauncher.bootstrap.ui.flatlaf;

import com.formdev.flatlaf.*;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.ui.UserInterface;
import net.legacylauncher.bootstrap.ui.flatlaf.themedetector.ThemeDetector;
import net.legacylauncher.util.shared.FlatLafConfiguration;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class FlatLaf {
    public static void initialize(FlatLafConfiguration config) {
        if(config.isEnabled()) {
            FlatLafConfiguration.Theme theme = config.getSelected().orElse(detectTheme());
            setUIProperties(config.getUiPropertiesFiles().get(theme));
            setLaf(theme, config.getThemeFiles().get(theme));
        } else {
            log.info("FlatLaf is not enabled. Skipping initialization");
        }
    }

    private static FlatLafConfiguration.Theme detectTheme() {
        log.info("Detecting system theme");
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
                        log.warn("Unknown theme id: {}", themeFile);
                        laf = new FlatLightLaf();
                        break;
                }
            } else {
                laf = loadLafFromThemeFile(themeFile);
            }
        }
        if (useSystemTheme) {
            log.info("System L&F is selected for theme {}", theme);
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
        log.info("Loading L&F theme from {}", themeFile);
        try(FileInputStream in = new FileInputStream(themeFile)) {
            return IntelliJTheme.createLaf(in);
        } catch (IOException e) {
            log.error("Couldn't load IntelliJ theme from file: {}", themeFile, e);
            return null;
        }
    }

    private static void setLaf(com.formdev.flatlaf.FlatLaf lookAndFeel) {
        log.info("Setting L&F: {}", lookAndFeel);
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            log.error("Couldn't set L&F", e);
        }
    }

    private static void setUIProperties(String uiPropertiesFile) {
        if(uiPropertiesFile == null) {
            log.info("No UI properties file, skipping");
            return;
        }
        log.info("Setting addon properties file: {}", uiPropertiesFile);
        Addon.PROPERTIES_FILE = uiPropertiesFile;
    }
}
