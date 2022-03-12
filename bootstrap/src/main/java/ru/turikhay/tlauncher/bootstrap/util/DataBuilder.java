package ru.turikhay.tlauncher.bootstrap.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DataBuilder {
    private final Map<String, String> data = new LinkedHashMap<>();

    public DataBuilder add(String key, String value) {
        data.put(key, value);
        return this;
    }

    public DataBuilder add(String key, Object value) {
        String str;

        if (value instanceof File) {
            str = ((File) value).getAbsolutePath();
        } else {
            str = String.valueOf(value);
        }

        return add(key, str);
    }

    public Map<String, String> build() {
        return data;
    }

    public static DataBuilder create(String key, Object value) {
        return new DataBuilder().add(key, value);
    }
}
