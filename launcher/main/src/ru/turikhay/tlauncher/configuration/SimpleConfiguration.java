package ru.turikhay.tlauncher.configuration;

import ru.turikhay.util.FileUtil;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

public class SimpleConfiguration implements AbstractConfiguration {
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
            U.log("Error loading config from file:", var3);
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
        Iterator var4 = map.entrySet().iterator();

        while (var4.hasNext()) {
            Entry en = (Entry) var4.next();
            String key = (String) en.getKey();
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
        HashSet set = new HashSet();
        Iterator var3 = properties.keySet().iterator();

        while (var3.hasNext()) {
            Object obj = var3.next();
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

    public void save() throws IOException {
        if (!isSaveable()) {
            throw new UnsupportedOperationException();
        } else {
            File file = (File) input;
            FileUtil.createFile(file);
            properties.store(new FileOutputStream(file), comments);
        }
    }

    public void store() {
        try {
            save();
        } catch (IOException var2) {
            U.log(var2);
        }
    }

    public void clear() {
        properties.clear();
    }

    public boolean isSaveable() {
        return input != null && input instanceof File;
    }

    private static void loadFromStream(Properties properties, InputStream stream) throws IOException {
        if (stream == null) {
            throw new NullPointerException();
        } else {
            InputStreamReader reader = new InputStreamReader(new BufferedInputStream(stream), Charset.forName("UTF-8"));
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
            FileInputStream stream = new FileInputStream(file);
            loadFromStream(properties, stream);
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

            Iterator var4 = src.entrySet().iterator();

            while (var4.hasNext()) {
                Entry en = (Entry) var4.next();
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
