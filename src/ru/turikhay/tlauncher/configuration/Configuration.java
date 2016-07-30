package ru.turikhay.tlauncher.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.util.Direction;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Configuration extends SimpleConfiguration {
   private static final List DEFAULT_LOCALES = getDefaultLocales();
   private ConfigurationDefaults defaults;
   private Map constants;
   private boolean firstRun;

   private Configuration(File file, OptionSet set) {
      super(file);
      this.init(set);
   }

   public static Configuration createConfiguration(OptionSet set) throws IOException {
      Object path = set != null ? set.valueOf("settings") : null;
      File file;
      if (path == null) {
         file = FileUtil.getNeighborFile("tlauncher.cfg");
         if (!file.isFile()) {
            file = FileUtil.getNeighborFile("tlauncher.properties");
         }

         if (!file.isFile()) {
            file = MinecraftUtil.getSystemRelatedDirectory(TLauncher.getSettingsFile());
         }
      } else {
         log("Fetching configuration from argument:", path);
         file = new File(path.toString());
      }

      boolean doesntExist = !file.isFile();
      if (doesntExist) {
         log("Creating file:", file);
         FileUtil.createFile(file);
      }

      log("File:", file);
      Configuration config = new Configuration(file, set);
      config.firstRun = doesntExist;
      return config;
   }

   private void init(OptionSet set) {
      this.comments = " TLauncher " + TLauncher.getBrand() + " properties\n Created in " + TLauncher.getVersion() + (TLauncher.isBeta() ? " BETA" : "");
      this.defaults = ConfigurationDefaults.getInstance();
      this.constants = ArgumentParser.parse(set);
      if (this.getDouble("settings.version") != (double)ConfigurationDefaults.getVersion()) {
         log("Configuration is being wiped due to version incapability");
         this.set("settings.version", ConfigurationDefaults.getVersion(), false);
         this.clear();
      }

      log("Constants:", this.constants);
      this.set(this.constants, false);
      Iterator var2 = this.defaults.getMap().entrySet().iterator();

      while(var2.hasNext()) {
         Entry defEntry = (Entry)var2.next();
         if (!this.constants.containsKey(defEntry.getKey())) {
            String value = this.get((String)defEntry.getKey());

            try {
               PlainParser.parse(this.get((String)defEntry.getKey()), defEntry.getValue());
            } catch (RuntimeException var7) {
               log("Could not parse", defEntry.getKey(), "; got:", value);
               this.set((String)defEntry.getKey(), defEntry.getValue(), false);
            }
         }
      }

      Locale locale = getLocaleOf(this.get("locale"));
      if (locale == null) {
         log("Presented locale is not supported by Java:", this.get("locale"));
         log("May be system default?");
         locale = Locale.getDefault();
      }

      if (!DEFAULT_LOCALES.contains(locale)) {
         log("We don't have localization for", locale);
         if (locale != getLocaleOf("uk_UA") && locale != getLocaleOf("be_BY")) {
            locale = Locale.US;
         } else {
            locale = getLocaleOf("ru_RU");
         }

         log("Selecting", locale);
      }

      this.set("locale", locale);
      int oldFontSize = this.getInteger("gui.font.old");
      if (oldFontSize == 0) {
         this.set("gui.font.old", this.getInteger("gui.font"));
      }

      log(this.properties.entrySet());
      if (this.isSaveable()) {
         try {
            this.save();
         } catch (IOException var6) {
            log("Couldn't save config", var6);
         }
      }

   }

   public boolean isFirstRun() {
      return this.firstRun;
   }

   public boolean isSaveable(String key) {
      return !this.constants.containsKey(key);
   }

   public Locale getLocale() {
      return getLocaleOf(this.get("locale"));
   }

   public boolean isUSSRLocale() {
      return isUSSRLocale(this.getLocale().toString());
   }

   public Locale[] getLocales() {
      return (Locale[])DEFAULT_LOCALES.toArray(new Locale[DEFAULT_LOCALES.size()]);
   }

   public Configuration.ActionOnLaunch getActionOnLaunch() {
      return Configuration.ActionOnLaunch.get(this.get("minecraft.onlaunch"));
   }

   public Configuration.LoggerType getLoggerType() {
      return Configuration.LoggerType.get(this.get("gui.logger"));
   }

   public Configuration.ConnectionQuality getConnectionQuality() {
      return Configuration.ConnectionQuality.get(this.get("connection"));
   }

   public int[] getClientWindowSize() {
      String plainValue = this.get("minecraft.size");
      int[] value = new int[2];
      if (plainValue == null) {
         return new int[2];
      } else {
         try {
            IntegerArray arr = IntegerArray.parseIntegerArray(plainValue);
            value[0] = arr.get(0);
            value[1] = arr.get(1);
         } catch (Exception var4) {
         }

         return value;
      }
   }

   public int[] getLauncherWindowSize() {
      String plainValue = this.get("gui.size");
      int[] value = new int[2];
      if (plainValue == null) {
         return new int[2];
      } else {
         try {
            IntegerArray arr = IntegerArray.parseIntegerArray(plainValue);
            value[0] = arr.get(0);
            value[1] = arr.get(1);
         } catch (Exception var4) {
         }

         return value;
      }
   }

   public int[] getDefaultClientWindowSize() {
      String plainValue = this.getDefault("minecraft.size");
      return IntegerArray.parseIntegerArray(plainValue).toArray();
   }

   public int[] getDefaultLauncherWindowSize() {
      String plainValue = this.getDefault("gui.size");
      return IntegerArray.parseIntegerArray(plainValue).toArray();
   }

   public VersionFilter getVersionFilter() {
      VersionFilter filter = new VersionFilter();
      Iterator var3 = ReleaseType.getDefinable().iterator();

      while(var3.hasNext()) {
         ReleaseType type = (ReleaseType)var3.next();
         boolean include = this.getBoolean("minecraft.versions." + type);
         if (!include) {
            filter.exclude(type);
         }
      }

      ReleaseType.SubType[] var5;
      int var9 = (var5 = ReleaseType.SubType.values()).length;

      for(int var8 = 0; var8 < var9; ++var8) {
         ReleaseType.SubType var7 = var5[var8];
         boolean include1 = this.getBoolean("minecraft.versions.sub." + var7);
         if (!include1) {
            filter.exclude(var7);
         }
      }

      return filter;
   }

   public Direction getDirection(String key) {
      return (Direction)Reflect.parseEnum(Direction.class, this.get(key));
   }

   public UUID getClient() {
      try {
         return UUID.fromString(this.get("client"));
      } catch (Exception var2) {
         return this.refreshClient();
      }
   }

   public UUID refreshClient() {
      UUID newId = UUID.randomUUID();
      this.set("client", newId);
      return newId;
   }

   public String getDefault(String key) {
      return this.getStringOf(this.defaults.get(key));
   }

   public void set(String key, Object value, boolean flush) {
      if (!this.constants.containsKey(key)) {
         super.set(key, value, flush);
      }

   }

   public void setForcefully(String key, Object value, boolean flush) {
      super.set(key, value, flush);
   }

   public void save() throws IOException {
      if (!this.isSaveable()) {
         throw new UnsupportedOperationException();
      } else {
         Properties temp = copyProperties(this.properties);
         Iterator var3 = this.constants.keySet().iterator();

         while(var3.hasNext()) {
            String file = (String)var3.next();
            temp.remove(file);
         }

         File file1 = (File)this.input;
         temp.store(new FileOutputStream(file1), this.comments);
      }
   }

   private static List getDefaultLocales() {
      ArrayList l = new ArrayList();
      String[] ll = Static.getLangList();
      String[] var2 = ll;
      int var3 = ll.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String locale = var2[var4];
         Locale loc = getLocaleOf(locale);
         if (loc == null) {
            throw new NullPointerException("unknown locale: " + locale);
         }

         l.add(loc);
      }

      return l;
   }

   public static boolean isUSSRLocale(String l) {
      return "ru_RU".equals(l) || "uk_UA".equals(l);
   }

   public static Locale getLocaleOf(String locale) {
      if (locale == null) {
         return null;
      } else {
         Locale[] var4;
         int var3 = (var4 = Locale.getAvailableLocales()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            Locale cur = var4[var2];
            if (cur.toString().equals(locale)) {
               return cur;
            }
         }

         return null;
      }
   }

   private static void log(Object... o) {
      U.log("[Config]", o);
   }

   public static enum LoggerType {
      GLOBAL,
      MINECRAFT,
      NONE;

      public static boolean parse(String val) {
         if (val == null) {
            return false;
         } else {
            Configuration.LoggerType[] var4;
            int var3 = (var4 = values()).length;

            for(int var2 = 0; var2 < var3; ++var2) {
               Configuration.LoggerType cur = var4[var2];
               if (cur.toString().equalsIgnoreCase(val)) {
                  return true;
               }
            }

            return false;
         }
      }

      public static Configuration.LoggerType get(String val) {
         Configuration.LoggerType[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            Configuration.LoggerType cur = var4[var2];
            if (cur.toString().equalsIgnoreCase(val)) {
               return cur;
            }
         }

         return null;
      }

      public MinecraftLauncher.LoggerVisibility getVisibility() {
         return this == GLOBAL ? MinecraftLauncher.LoggerVisibility.NONE : (this == MINECRAFT ? MinecraftLauncher.LoggerVisibility.ALWAYS : MinecraftLauncher.LoggerVisibility.ON_CRASH);
      }

      public String toString() {
         return super.toString().toLowerCase();
      }

      public static Configuration.LoggerType getDefault() {
         return NONE;
      }
   }

   public static enum ConnectionQuality {
      GOOD(2, 5, 6, 15000),
      NORMAL(5, 10, 3, 45000),
      BAD(10, 20, 1, 120000);

      private final int minTries;
      private final int maxTries;
      private final int maxThreads;
      private final int timeout;
      private final int[] configuration;

      private ConnectionQuality(int minTries, int maxTries, int maxThreads, int timeout) {
         this.minTries = minTries;
         this.maxTries = maxTries;
         this.maxThreads = maxThreads;
         this.timeout = timeout;
         this.configuration = new int[]{minTries, maxTries, maxThreads};
      }

      public static boolean parse(String val) {
         if (val == null) {
            return false;
         } else {
            Configuration.ConnectionQuality[] var4;
            int var3 = (var4 = values()).length;

            for(int var2 = 0; var2 < var3; ++var2) {
               Configuration.ConnectionQuality cur = var4[var2];
               if (cur.toString().equalsIgnoreCase(val)) {
                  return true;
               }
            }

            return false;
         }
      }

      public static Configuration.ConnectionQuality get(String val) {
         Configuration.ConnectionQuality[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            Configuration.ConnectionQuality cur = var4[var2];
            if (cur.toString().equalsIgnoreCase(val)) {
               return cur;
            }
         }

         return null;
      }

      public int getMaxTries() {
         return this.maxTries;
      }

      public int getMaxThreads() {
         return this.maxThreads;
      }

      public int getTries(boolean fast) {
         return fast ? this.minTries : this.maxTries;
      }

      public int getTimeout() {
         return this.timeout;
      }

      public String toString() {
         return super.toString().toLowerCase();
      }

      public static Configuration.ConnectionQuality getDefault() {
         return GOOD;
      }
   }

   public static enum ActionOnLaunch {
      HIDE,
      EXIT,
      NOTHING;

      public static boolean parse(String val) {
         if (val == null) {
            return false;
         } else {
            Configuration.ActionOnLaunch[] var4;
            int var3 = (var4 = values()).length;

            for(int var2 = 0; var2 < var3; ++var2) {
               Configuration.ActionOnLaunch cur = var4[var2];
               if (cur.toString().equalsIgnoreCase(val)) {
                  return true;
               }
            }

            return false;
         }
      }

      public static Configuration.ActionOnLaunch get(String val) {
         Configuration.ActionOnLaunch[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            Configuration.ActionOnLaunch cur = var4[var2];
            if (cur.toString().equalsIgnoreCase(val)) {
               return cur;
            }
         }

         return null;
      }

      public String toString() {
         return super.toString().toLowerCase();
      }
   }
}
