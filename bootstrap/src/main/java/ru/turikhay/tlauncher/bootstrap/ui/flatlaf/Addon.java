package ru.turikhay.tlauncher.bootstrap.ui.flatlaf;

import com.formdev.flatlaf.FlatDefaultsAddon;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Addon extends FlatDefaultsAddon {
    public static String PROPERTIES_FILE;

    @Override
    public InputStream getDefaults(Class<?> lafClass) {
        if (PROPERTIES_FILE != null) {
            log("Loading properties file: ", PROPERTIES_FILE);
            try {
                return new FileInputStream(PROPERTIES_FILE);
            } catch (FileNotFoundException e) {
                log("Couldn't load ", PROPERTIES_FILE, e);
            }
        }
        return null;
    }

    private static void log(Object... o) {
        U.log("[FlatLafAddon]", o);
    }
}
