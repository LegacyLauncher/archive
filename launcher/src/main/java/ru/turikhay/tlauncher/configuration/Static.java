package ru.turikhay.tlauncher.configuration;

import ru.turikhay.util.U;

public final class Static {
    private static final String SETTINGS = "tlauncher/legacy.properties";
    private static final String BRAND = "Legacy";
    private static final String FOLDER = "minecraft";
    private static final String[] OFFICIAL_REPO = {"https://s3.amazonaws.com/Minecraft.Download/"};
    private static final String[] EXTRA_REPO = U.shuffle("http://u.tlauncher.ru/repo/", "http://turikhay.ru/tlauncher/repo/", "http://tlauncherrepo.com/repo/");
    private static final String[] LIBRARY_REPO = {"https://libraries.minecraft.net/", "https://u.tlauncher.ru/repo/libraries/", "https://turikhay.ru/tlauncher/repo/libraries/", "https://tlauncherrepo.com/repo/libraries/"};
    private static final String[] ASSETS_REPO = {"https://resources.download.minecraft.net/"};
    private static final String[] SERVER_LIST = {};
    private static final String[] LANG_LIST = {"en_US", "ru_RU", "uk_UA"};

    public static String getSettings() {
        return SETTINGS;
    }

    public static String getBrand() {
        return BRAND;
    }

    public static String getFolder() {
        return FOLDER;
    }

    public static String[] getOfficialRepo() {
        return OFFICIAL_REPO;
    }

    public static String[] getExtraRepo() {
        return EXTRA_REPO;
    }

    public static String[] getLibraryRepo() {
        return LIBRARY_REPO;
    }

    public static String[] getAssetsRepo() {
        return ASSETS_REPO;
    }

    public static String[] getServerList() {
        return SERVER_LIST;
    }

    public static String[] getLangList() {
        return LANG_LIST;
    }

    private Static() {
    }
}