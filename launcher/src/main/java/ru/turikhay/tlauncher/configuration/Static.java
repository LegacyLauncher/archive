package ru.turikhay.tlauncher.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Static {
    private static final String BRAND = BuildConfig.FULL_BRAND;
    private static final String SHORT_BRAND = BuildConfig.SHORT_BRAND;
    private static final String SETTINGS = "tlauncher/" + (isLegacy() ? "legacy" : SHORT_BRAND) + ".properties";
    private static final String FOLDER = "minecraft";
    private static final List<String> OFFICIAL_REPO = Collections.singletonList("https://s3.amazonaws.com/Minecraft.Download/");
    private static final List<String> EXTRA_REPO;
    private static final List<String> LIBRARY_REPO = Collections.unmodifiableList(Arrays.asList("https://libraries.minecraft.net/", "https://tln4.ru/repo/libraries/", "https://repo.tlaun.ch/repo/libraries/", "https://tlauncherrepo.com/repo/libraries/"));
    private static final List<String> ASSETS_REPO;

    static {
        List<String> assets = new ArrayList<>(3);
        assets.add("https://resources.download.mcproxy.tlaun.ch/");
        assets.add("https://resources.download.mcproxy.tln4.ru/");
        Collections.shuffle(assets);
        assets.add(0, "https://resources.download.minecraft.net/");
        ASSETS_REPO = Collections.unmodifiableList(assets);

        List<String> extra = Arrays.asList("https://tln4.ru/repo/", "https://repo.tlaun.ch/repo/", "https://tlauncherrepo.com/repo/");
        Collections.shuffle(extra);
        EXTRA_REPO = Collections.unmodifiableList(extra);
    }

    private static final List<String> SERVER_LIST = Collections.emptyList();
    private static final List<String> LANG_LIST = Collections.unmodifiableList(Arrays.asList("en_US", "ru_RU", "uk_UA", "pt_BR", "vi", "tr_TR", "fr_FR", "id_ID", "pl_PL", "it_IT", "de_DE", "ro_RO", "zh_CN"));

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

    public static List<String> getLangList() {
        return LANG_LIST;
    }

    private Static() {
    }
}
