package ru.turikhay.tlauncher.configuration;

import ru.turikhay.util.U;

public final class Static {
   private static final String[] UPDATE_REPO = new String[]{"http://cdn.turikhay.ru/tlauncher/update/legacy.json.gzip", "http://u.tlauncher.ru/update/legacy.json", "http://repo.tlauncher.ru/update/legacy.json", "http://turikhay.ru/tlauncher/update/legacy.json", "http://tlaun.ch/update/legacy.json"};
   private static final String[] BETA_UPDATE_REPO = new String[0];
   private static final String[] OFFICIAL_REPO = new String[]{"http://s3.amazonaws.com/Minecraft.Download/"};
   private static final String[] EXTRA_REPO = (String[])U.shuffle("http://u.tlauncher.ru/repo/", "http://repo.tlauncher.ru/repo/", "http://turikhay.ru/tlauncher/repo/");
   private static final String[] LIBRARY_REPO = new String[]{"https://libraries.minecraft.net/"};
   private static final String[] ASSETS_REPO = new String[]{"http://resources.download.minecraft.net/"};
   private static final String[] SERVER_LIST = new String[0];
   private static final String[] LANG_LIST = new String[]{"en_US", "ru_RU"};

   public static String getSettings() {
      return "tlauncher/legacy.properties";
   }

   public static String getBrand() {
      return "Legacy";
   }

   public static String getFolder() {
      return "minecraft";
   }

   public static String[] getUpdateRepo() {
      return UPDATE_REPO;
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
}
