package ru.turikhay.util;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DataBuilder {
    private final Map<String, String> data = new LinkedHashMap<String, String>();

    public DataBuilder add(Map<String, String> data) {
        if(data != null) {
            this.data.putAll(data);
        }
        return this;
    }

    public DataBuilder add(String key, String value) {
        data.put(key, value);
        return this;
    }

    public DataBuilder add(String key, Object value) {
        return add(key, U.toLog(value));
    }

    public DataBuilder add(DataBuilder dataBuilder) {
        if(dataBuilder != null) {
            data.putAll(dataBuilder.data);
        }
        return this;
    }

    public Map<String, String> build() {
        return data;
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public static DataBuilder create(String key, Object value) {
        return new DataBuilder().add(key, value);
    }
}
