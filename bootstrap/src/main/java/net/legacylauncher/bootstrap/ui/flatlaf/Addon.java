package net.legacylauncher.bootstrap.ui.flatlaf;

import com.formdev.flatlaf.FlatDefaultsAddon;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
public class Addon extends FlatDefaultsAddon {
    public static String PROPERTIES_FILE;

    @Override
    public InputStream getDefaults(Class<?> lafClass) {
        if (PROPERTIES_FILE != null) {
            log.info("Loading properties file: {}", PROPERTIES_FILE);
            try {
                return new FileInputStream(PROPERTIES_FILE);
            } catch (FileNotFoundException e) {
                log.error("Couldn't load {}", PROPERTIES_FILE, e);
            }
        }
        return null;
    }
}
