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
   private ConfigurationDefaults defaults;
   private Map constants;
   private List defaultLocales;
   private List supportedLocales;
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
         file = new File(path.toString());
      }

      boolean doesntExist = !file.isFile();
      if (doesntExist) {
         U.log("Creating configuration file...");
         FileUtil.createFile(file);
      }

      U.log("Loading configuration from file:", file);
      Configuration config = new Configuration(file, set);
      config.firstRun = doesntExist;
      return config;
   }

   private void init(OptionSet set) {
      this.comments = " TLauncher configuration file\n Created in " + TLauncher.getBrand() + " " + TLauncher.getVersion();
      this.defaults = new ConfigurationDefaults();
      this.constants = ArgumentParser.parse(set);
      this.set(this.constants, false);
      this.log(new Object[]{"Constant values:", this.constants});
      int version = ConfigurationDefaults.getVersion();
      if (this.getDouble("settings.version") != (double)version) {
         this.clear();
      }

      this.set("settings.version", version, false);
      Iterator oldJavaPath = this.defaults.getMap().entrySet().iterator();

      String oldJavaPath1;
      while(oldJavaPath.hasNext()) {
         Entry selected = (Entry)oldJavaPath.next();
         oldJavaPath1 = (String)selected.getKey();
         if (this.constants.containsKey(oldJavaPath1)) {
            this.log(new Object[]{"Key \"" + oldJavaPath1 + "\" is unsaveable!"});
         } else {
            String value = this.get(oldJavaPath1);
            Object defvalue = selected.getValue();
            if (defvalue != null) {
               try {
                  PlainParser.parse(value, defvalue);
               } catch (Exception var10) {
                  this.log(new Object[]{"Key \"" + oldJavaPath1 + "\" is invalid!", var10});
                  this.set(oldJavaPath1, defvalue, false);
               }
            }
         }
      }

      this.defaultLocales = getDefaultLocales();
      this.supportedLocales = this.getSupportedLocales();
      Locale selected1 = getLocaleOf(this.get("locale"));
      if (selected1 == null) {
         this.log(new Object[]{"Selected locale is not supported, trying system default"});
         selected1 = Locale.getDefault();
         if (selected1 == getLocaleOf("uk_UA") || selected1 == getLocaleOf("be_BY")) {
            selected1 = getLocaleOf("ru_RU");
         }
      }

      if (!this.supportedLocales.contains(selected1)) {
         this.log(new Object[]{"System default locale is not supported, applying en_US"});
         selected1 = Locale.US;
      }

      this.set("locale", selected1, false);
      oldJavaPath1 = this.get("minecraft.javadir");
      if (oldJavaPath1 != null) {
         this.log(new Object[]{"Migrating Java path into Command:", oldJavaPath1});
         this.set("minecraft.cmd", oldJavaPath1);
         this.set("minecraft.javadir", (Object)null);
      }

      int oldFontSize = this.getInteger("gui.font.old");
      if (oldFontSize == 0) {
         this.set("gui.font.old", this.getInteger("gui.font"));
      }

      if (this.isSaveable()) {
         try {
            this.save();
         } catch (IOException var9) {
            this.log(new Object[]{"Cannot save value!", var9});
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
      String locale = this.get("locale");
      return "ru_RU".equals(locale) || "uk_UA".equals(locale);
   }

   public Locale[] getLocales() {
      Locale[] locales = new Locale[this.supportedLocales.size()];
      return (Locale[])this.supportedLocales.toArray(locales);
   }

   public Configuration.ActionOnLaunch getActionOnLaunch() {
      return Configuration.ActionOnLaunch.get(this.get("minecraft.onlaunch"));
   }

   public Configuration.ConsoleType getConsoleType() {
      return Configuration.ConsoleType.get(this.get("gui.console"));
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

   private List getSupportedLocales() {
      return this.defaultLocales;
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

   public static enum ConsoleType {
      GLOBAL,
      MINECRAFT,
      NONE;

      public static boolean parse(String val) {
         if (val == null) {
            return false;
         } else {
            Configuration.ConsoleType[] var4;
            int var3 = (var4 = values()).length;

            for(int var2 = 0; var2 < var3; ++var2) {
               Configuration.ConsoleType cur = var4[var2];
               if (cur.toString().equalsIgnoreCase(val)) {
                  return true;
               }
            }

            return false;
         }
      }

      public static Configuration.ConsoleType get(String val) {
         Configuration.ConsoleType[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            Configuration.ConsoleType cur = var4[var2];
            if (cur.toString().equalsIgnoreCase(val)) {
               return cur;
            }
         }

         return null;
      }

      public MinecraftLauncher.ConsoleVisibility getVisibility() {
         return this == GLOBAL ? MinecraftLauncher.ConsoleVisibility.NONE : (this == MINECRAFT ? MinecraftLauncher.ConsoleVisibility.ALWAYS : MinecraftLauncher.ConsoleVisibility.ON_CRASH);
      }

      public String toString() {
         return super.toString().toLowerCase();
      }

      public static Configuration.ConsoleType getDefault() {
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

      public static Configuration.ActionOnLaunch getDefault() {
         return HIDE;
      }
   }
}
