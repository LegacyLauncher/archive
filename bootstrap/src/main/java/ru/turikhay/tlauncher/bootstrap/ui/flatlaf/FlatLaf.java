package ru.turikhay.tlauncher.bootstrap.ui.flatlaf;

import com.formdev.flatlaf.*;
import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;
import ru.turikhay.tlauncher.bootstrap.ui.flatlaf.themedetector.ThemeDetector;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration;
import ru.turikhay.tlauncher.bootstrap.bridge.FlatLafConfiguration.Theme;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;

public class FlatLaf {

    public static void initialize(FlatLafConfiguration config) {
        if(config.isEnabled()) {
            Theme theme = config.getSelected().orElse(detectTheme());
            setUIProperties(config.getUiPropertiesFiles().get(theme));
            setLaf(theme, config.getThemeFiles().get(theme));
        } else {
            log("FlatLaf is not enabled. Skipping initialization");
        }
    }

    private static Theme detectTheme() {
        log("Detecting system theme");
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
                        log("unknown theme id", themeFile);
                        laf = new FlatLightLaf();
                        break;
                }
            } else {
                laf = loadLafFromThemeFile(themeFile);
            }
        }
        if (useSystemTheme) {
            log("System L&F is selected for theme ", theme);
            if (theme == Theme.DARK) {
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
        log("Loading L&F theme from", themeFile);
        try(FileInputStream in = new FileInputStream(themeFile)) {
            return IntelliJTheme.createLaf(in);
        } catch (IOException e) {
            log("Couldn't load IntelliJ theme from file:", themeFile, e);
            return null;
        }
    }

    private static void setLaf(com.formdev.flatlaf.FlatLaf lookAndFeel) {
        log("Setting L&F:", lookAndFeel);
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            log("Couldn't set L&F", e);
        }
//        if (updateWindows) {
//            updateLafInWindows();
//        }
    }

    private static void setUIProperties(String uiPropertiesFile) {
        if(uiPropertiesFile == null) {
            log("No UI properties file, skipping");
            return;
        }
        log("Setting addon properties file: ", uiPropertiesFile);
        Addon.PROPERTIES_FILE = uiPropertiesFile;
    }

//    private static void updateLafInWindows() {
//        for (Window window : Window.getWindows()) {
//            updateLafInWindowsRecursively(window);
//        }
//    }
//
//    private static void updateLafInWindowsRecursively(Window window) {
//        for (Window childWindow : window.getOwnedWindows()) {
//            updateLafInWindowsRecursively(childWindow);
//        }
//        SwingUtilities.updateComponentTreeUI(window);
//    }

    private static void log(Object... o) {
        U.log("[FlatLaf]", o);
    }
}
