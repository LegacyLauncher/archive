package net.legacylauncher.bootstrap.ui.flatlaf;

import com.formdev.flatlaf.FlatDefaultsAddon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Addon extends FlatDefaultsAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger(Addon.class);
    public static String PROPERTIES_FILE;

    @Override
    public InputStream getDefaults(Class<?> lafClass) {
        if (PROPERTIES_FILE != null) {
            LOGGER.info("Loading properties file: {}", PROPERTIES_FILE);
            try {
                return new FileInputStream(PROPERTIES_FILE);
            } catch (FileNotFoundException e) {
                LOGGER.error("Couldn't load {}", PROPERTIES_FILE, e);
            }
        }
        return null;
    }
}
