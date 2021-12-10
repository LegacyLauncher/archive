package ru.turikhay.tlauncher.configuration;

import ru.turikhay.util.U;

import java.util.Arrays;
import java.util.LinkedHashSet;

public final class Static {
    private static final String SETTINGS = "tlauncher/"+ (isLegacy()? "legacy" : "@tl_short_brand@") +".properties";
    private static final String BRAND = "@tl_brand@";
    private static final String SHORT_BRAND = "@tl_short_brand@";
    private static final String FOLDER = "minecraft";
    private static final String[] OFFICIAL_REPO = {"https://s3.amazonaws.com/Minecraft.Download/"};
    private static final String[] EXTRA_REPO = U.shuffle("https://u.tlauncher.ru/repo/", "https://repo.tlaun.ch/repo/", "https://tlauncherrepo.com/repo/");
    private static final String[] LIBRARY_REPO = {"https://libraries.minecraft.net/", "https://u.tlauncher.ru/repo/libraries/", "https://repo.tlaun.ch/repo/libraries/", "https://tlauncherrepo.com/repo/libraries/"};
    private static final String[] ASSETS_REPO = {"https://resources.download.minecraft.net/"};
    private static final String[] SERVER_LIST = {};
    private static final String[] LANG_LIST = generateLangList();

    private static boolean isLegacy() {
        return getShortBrand().startsWith("legacy");
    }

    private static boolean isAUR() {
        return getShortBrand().startsWith("aur");
    }

    private static String[] generateLangList() {
        LinkedHashSet<String> legacy_langs = new LinkedHashSet<>(Arrays.asList("en_US", "ru_RU", "uk_UA"));
        LinkedHashSet<String> default_langs = new LinkedHashSet<>((Arrays.asList("en_US", "pt_BR", "vi", "id_ID", "de_DE", "pl_PL", "ro_RO", "fr_FR", "it_IT", "tr_TR", "zh_CN")));

        if (isLegacy()) {
            return legacy_langs.toArray(new String[0]);
        }

        if (isAUR()) {
            default_langs.addAll(legacy_langs);
            return default_langs.toArray(new String[0]);
        }

        return default_langs.toArray(new String[0]);
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
