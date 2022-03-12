package net.minecraft.launcher.versions;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;

import java.util.HashMap;

public enum ModpackType {
    FORGE_LEGACY("legacy"),
    FORGE_LEGACY_ABSOLUTE("1.12.2_bug"),
    FORGE_1_13("aquatic"),
    NONE("none");

    private final String name;
    private static final HashMap<String, ModpackType> list;
    private static final Version version_1_13;
    private static final Version version_1_12_2;
    private static final Version version_1_7_10;

    ModpackType(String name) {
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
        for (ModpackType value : values()) {
            list.put(value.name, value);
        }

        version_1_13 = Version.valueOf("1.13.0");
        version_1_12_2 = Version.valueOf("1.12.2");
        version_1_7_10 = Version.valueOf("1.7.10");
    }

    public static ModpackType getByName(String name, String version) {
        return list.getOrDefault(name, getByVersion(version));
    }

    private static ModpackType getByVersion(String version) {
        if (version == null) return NONE; // костыль для костыля для ванилек
        if (version.matches("^\\d\\.\\d+$")) version += ".0"; // костыль для SemVer

        Version semVer;
        try {
            semVer = Version.valueOf(version);
        } catch (ParseException e) {
            return NONE;
        }

        if (semVer.greaterThanOrEqualTo(version_1_13))
            return FORGE_1_13;

        if (semVer.equals(version_1_12_2))
            return FORGE_LEGACY_ABSOLUTE;

        if (semVer.greaterThanOrEqualTo(version_1_7_10))
            return FORGE_LEGACY;

        return NONE;
    }
}
