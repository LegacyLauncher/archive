package ru.turikhay.tlauncher.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.StringUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleConfiguration implements AbstractConfiguration {
    private final static Logger LOGGER = LogManager.getLogger();

    protected final Properties properties;
    protected Object input;
    protected String comments;

    public SimpleConfiguration() {
        properties = new Properties();
    }

    public SimpleConfiguration(InputStream stream) throws IOException {
        this();
        loadFromStream(properties, stream);
        input = stream;
    }

    public SimpleConfiguration(File file) {
        this();

        try {
            loadFromFile(properties, file);
        } catch (Exception var3) {
            LOGGER.warn("Error loading config from file: {}", file, var3);
        }

        input = file;
    }

    public SimpleConfiguration(URL url) throws IOException {
        this();
        loadFromURL(properties, url);
        input = url;
    }

    public String get(String key) {
        return getStringOf(properties.getProperty(key));
    }

    protected String getStringOf(Object obj) {
        String s;
        if (obj == null) {
            s = null;
        } else {
            s = obj.toString();
            if (s.isEmpty()) {
                s = null;
            }
        }

        return s;
    }

    public void set(String key, Object value, boolean flush) {
        if (key == null) {
            throw new NullPointerException();
        } else {
            if (value == null) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value.toString());
            }

            if (flush && isSaveable()) {
                store();
            }

        }
    }

    public void set(String key, Object value) {
        set(key, value, true);
    }

    public void set(Map<String, Object> map, boolean flush) {

        for (Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object value = en.getValue();
            if (value == null) {
                properties.remove(key);
            } else {
                properties.setProperty(key, value.toString());
            }
        }

        if (flush && isSaveable()) {
            store();
        }
    }

    public void set(Map<String, Object> map) {
        set(map, false);
    }

    public Set<String> getKeys() {
        Set<String> set = new HashSet<>();

        for (Object obj : properties.keySet()) {
            set.add(getStringOf(obj));
        }

        return Collections.unmodifiableSet(set);
    }

    public String getDefault(String key) {
        return null;
    }

    public int getInteger(String key, int def) {
        return getIntegerOf(get(key), 0);
    }

    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    protected int getIntegerOf(Object obj, int def) {
        try {
            return Integer.parseInt(obj.toString());
        } catch (Exception var4) {
            return def;
        }
    }

    public double getDouble(String key) {
        return getDoubleOf(get(key), 0.0D);
    }

    protected double getDoubleOf(Object obj, double def) {
        try {
            return Double.parseDouble(obj.toString());
        } catch (Exception var5) {
            return def;
        }
    }

    public float getFloat(String key) {
        return getFloatOf(get(key), 0.0F);
    }

    protected float getFloatOf(Object obj, float def) {
        try {
            return Float.parseFloat(obj.toString());
        } catch (Exception var4) {
            return def;
        }
    }

    public long getLong(String key) {
        return getLongOf(get(key), 0L);
    }

    protected long getLongOf(Object obj, long def) {
        try {
            return Long.parseLong(obj.toString());
        } catch (Exception var5) {
            return def;
        }
    }

    public boolean getBoolean(String key) {
        return getBooleanOf(get(key), false);
    }

    protected boolean getBooleanOf(Object obj, boolean def) {
        try {
            return StringUtil.parseBoolean(obj.toString());
        } catch (Exception var4) {
            return def;
        }
    }

    public int getDefaultInteger(String key) {
        return 0;
    }

    public double getDefaultDouble(String key) {
        return 0.0D;
    }

    public float getDefaultFloat(String key) {
        return 0.0F;
    }

    public long getDefaultLong(String key) {
        return 0L;
    }

    public boolean getDefaultBoolean(String key) {
        return false;
    }

    public synchronized void save() throws IOException {
        if (!isSaveable()) {
            throw new UnsupportedOperationException();
        }
        Properties propsToSave = processSavingProperties(properties);
        File file = (File) input;
        File tmpFile = new File(file.getAbsolutePath() + ".tmp");
        FileUtil.createFile(tmpFile);
        try (FileOutputStream stream = new FileOutputStream(tmpFile)) {
            propsToSave.store(stream, comments);
        }
        Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    protected Properties processSavingProperties(Properties og) {
        return og;
    }

    public void store() {
        try {
            save();
        } catch (IOException e) {
            LOGGER.warn("Couldn't save configuration to {}", input, e);
        }
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return properties.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return properties.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return get(String.valueOf(key));
    }

    @Override
    public String put(String key, String value) {
        set(key, value);
        return null;
    }

    @Override
    public String remove(Object key) {
        set(String.valueOf(key), null);
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        properties.clear();
    }

    @Override
    public Set<String> keySet() {
        return properties.keySet().stream().map(String::valueOf).collect(Collectors.toSet());
    }

    @Override
    public Collection<String> values() {
        return properties.values().stream().map(String::valueOf).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return null;
    }

    public boolean isSaveable() {
        return input != null && input instanceof File;
    }

    private static void loadFromStream(Properties properties, InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        } else {
            InputStreamReader reader = new InputStreamReader(new BufferedInputStream(stream), StandardCharsets.UTF_8);
            properties.clear();
            properties.load(reader);
        }
    }

    static Properties loadFromStream(InputStream stream) throws IOException {
        Properties properties = new Properties();
        loadFromStream(properties, stream);
        return properties;
    }

    private static void loadFromFile(Properties properties, File file) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        } else {
            try (FileInputStream stream = new FileInputStream(file)) {
                loadFromStream(properties, stream);
            }
        }
    }

    protected static Properties loadFromFile(File file) throws IOException {
        Properties properties = new Properties();
        loadFromFile(properties, file);
        return properties;
    }

    private static void loadFromURL(Properties properties, URL url) throws IOException {
        if (url == null) {
            throw new NullPointerException();
        } else {
            InputStream connection = url.openStream();
            loadFromStream(properties, connection);
        }
    }

    protected static Properties loadFromURL(URL url) throws IOException {
        Properties properties = new Properties();
        loadFromURL(properties, url);
        return properties;
    }

    protected static void copyProperties(Properties src, Properties dest, boolean wipe) {
        if (src == null) {
            throw new NullPointerException("src is NULL");
        } else if (dest == null) {
            throw new NullPointerException("dest is NULL");
        } else {
            if (wipe) {
                dest.clear();
            }

            for (Entry<Object, Object> en : src.entrySet()) {
                String key = en.getKey() == null ? null : en.getKey().toString();
                String value = en.getKey() == null ? null : en.getValue().toString();
                dest.setProperty(key, value);
            }

        }
    }

    protected static Properties copyProperties(Properties src) {
        Properties properties = new Properties();
        copyProperties(src, properties, false);
        return properties;
    }
}
