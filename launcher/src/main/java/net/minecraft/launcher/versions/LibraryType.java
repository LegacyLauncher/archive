package net.minecraft.launcher.versions;

import java.util.HashMap;

public enum LibraryType {
    LIBRARY("library"),
    MODIFICATION("mod"),
    TRANSFORMER("forge_transformer"),
    DOWNLOAD_ONLY("download_only");

    private final String name;
    private static final HashMap<String, LibraryType> list;

    LibraryType(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    static {
        list = new HashMap<>(values().length);
        for (LibraryType value : values()) {
            list.put(value.name, value);
        }
    }

    public static LibraryType getByName(String name) {
        return list.getOrDefault(name, LIBRARY);
    }
}
