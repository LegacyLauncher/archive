package net.legacylauncher.ui;

import com.formdev.flatlaf.FlatDefaultsAddon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FlatLafAddon extends FlatDefaultsAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlatLafAddon.class);
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
