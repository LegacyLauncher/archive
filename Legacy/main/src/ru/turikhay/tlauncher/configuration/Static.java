package ru.turikhay.tlauncher.configuration;

import ru.turikhay.util.U;

public final class Static {
    private static final String SETTINGS = "tlauncher/legacy.properties";
    private static final String BRAND = "Legacy";
    private static final String FOLDER = "minecraft";
    private static final String[] UPDATE_REPO = {"http://cdn.turikhay.ru/tlauncher/update/legacy.json.gzip", "http://u.tlauncher.ru/update/legacy.json", "http://repo.tlauncher.ru/update/legacy.json", "http://turikhay.ru/tlauncher/update/legacy.json", "http://tlaun.ch/update/legacy.json"};
    private static final String[] BETA_UPDATE_REPO = {};
    private static final String[] OFFICIAL_REPO = {"http://s3.amazonaws.com/Minecraft.Download/"};
    private static final String[] EXTRA_REPO = U.shuffle("http://u.tlauncher.ru/repo/", "http://repo.tlauncher.ru/repo/", "http://turikhay.ru/tlauncher/repo/");
    private static final String[] LIBRARY_REPO = {"https://libraries.minecraft.net/"};
    private static final String[] ASSETS_REPO = {"http://resources.download.minecraft.net/"};
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

    public static String[] getUpdateRepo() {
        return UPDATE_REPO;
    }

    public static String[] getBetaUpdateRepo() {
        return BETA_UPDATE_REPO;
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