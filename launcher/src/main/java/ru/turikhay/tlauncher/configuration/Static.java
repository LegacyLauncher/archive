package ru.turikhay.tlauncher.configuration;


import ru.turikhay.tlauncher.repository.RepoPrefixV1;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class Static {
    private static final String BRAND = BuildConfig.FULL_BRAND;
    private static final String SHORT_BRAND = BuildConfig.SHORT_BRAND;
    private static final String SETTINGS = "tlauncher/" + (isLegacy() ? "legacy" : SHORT_BRAND) + ".properties";
    private static final String FOLDER = "minecraft";
    private static final List<String> OFFICIAL_REPO = Collections.singletonList("https://s3.amazonaws.com/Minecraft.Download/");
    private static final List<String> EXTRA_REPO;
    private static final List<String> LIBRARY_REPO;
    private static final List<String> ASSETS_REPO;

    static {
        EXTRA_REPO = Collections.unmodifiableList(
                RepoPrefixV1.prefixesCdnLast()
                        .stream()
                        .map(prefix -> prefix + "/repo/")
                        .collect(Collectors.toList())
        );
        LIBRARY_REPO = Collections.unmodifiableList(
                RepoPrefixV1.combine(
                        Collections.singletonList("https://libraries.minecraft.net/"),
                        RepoPrefixV1.prefixesCdnLast()
                                .stream()
                                .map(prefix -> prefix + "/repo/libraries/")
                                .collect(Collectors.toList())
                )
        );
        ASSETS_REPO = Collections.unmodifiableList(
                RepoPrefixV1.combine(
                    Collections.singletonList("https://resources.download.minecraft.net/"),
                    RepoPrefixV1.prefixesCdnLast()
                            .stream()
                            .map(prefix -> prefix + "/proxy/assets/")
                            .collect(Collectors.toList())
                )
        );
    }

    private static final List<String> SERVER_LIST = Collections.emptyList();

    private static boolean isLegacy() {
        return getShortBrand().startsWith("legacy");
    }

    public static String getSettings() {
        return SETTINGS;
    }

    public static String getBrand() {
        return BRAND;
    }

    public static String getShortBrand() {
        return SHORT_BRAND;
    }

    public static String getFolder() {
        return FOLDER;
    }

    public static List<String> getOfficialRepo() {
        return OFFICIAL_REPO;
    }

    public static List<String> getExtraRepo() {
        return EXTRA_REPO;
    }

    public static List<String> getLibraryRepo() {
        return LIBRARY_REPO;
    }

    public static List<String> getAssetsRepo() {
        return ASSETS_REPO;
    }

    public static List<String> getServerList() {
        return SERVER_LIST;
    }

    private Static() {
    }
}
