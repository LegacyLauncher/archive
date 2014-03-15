package com.turikhay.tlauncher.configuration;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.exceptions.ParseException;
import com.turikhay.util.FileUtil;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import joptsimple.OptionSet;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;

public class Configuration extends SimpleConfiguration {
   private ConfigurationDefaults defaults;
   private Map constants;
   private List defaultLocales;
   private List supportedLocales;
   private boolean firstRun;

   private Configuration(URL url, OptionSet set) throws IOException {
      super(url);
      this.init(set);
   }

   private Configuration(File file, OptionSet set) {
      super(file);
      this.init(set);
   }

   public static Configuration createConfiguration(OptionSet set) throws IOException {
      Object path = set != null ? set.valueOf("settings") : null;
      String defaultName = TLauncher.getSettingsFile();
      File file;
      if (path == null) {
         File neighbor = FileUtil.getNeighborFile(defaultName);
         if (neighbor.isFile()) {
            file = neighbor;
         } else {
            file = MinecraftUtil.getSystemRelatedFile(defaultName);
         }
      } else {
         file = new File(path.toString());
      }

      U.log("Loading configuration from file:", file);
      boolean firstRun = !file.exists();
      Configuration config = new Configuration(file, set);
      config.firstRun = firstRun;
      return config;
   }

   public static Configuration createConfiguration() throws IOException {
      return createConfiguration((OptionSet)null);
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
      Iterator var4 = this.defaults.getMap().entrySet().iterator();

      while(var4.hasNext()) {
         Entry curen = (Entry)var4.next();
         String key = (String)curen.getKey();
         if (this.constants.containsKey(key)) {
            this.log(new Object[]{"Key \"" + key + "\" is unsaveable!"});
         } else {
            String value = this.get(key);
            Object defvalue = curen.getValue();
            if (defvalue != null) {
               try {
                  PlainParser.parse(value, defvalue);
               } catch (ParseException var10) {
                  this.log(new Object[]{"Key \"" + key + "\" is invalid!", var10});
                  this.set(key, defvalue, false);
               }
            }
         }
      }

      this.defaultLocales = this.getDefaultLocales();
      this.supportedLocales = this.getSupportedLocales();
      Locale selected = getLocaleOf(this.get("locale"));
      if (selected == null) {
         this.log(new Object[]{"Selected locale is not supported, rolling back to default one"});
         selected = Locale.getDefault();
      }

      if (!this.supportedLocales.contains(selected)) {
         this.log(new Object[]{"Default locale is not supported, rolling back to global default one"});
         selected = Locale.US;
      }

      this.set("locale", selected, false);
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

   public int[] getWindowSize() {
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

   public int[] getDefaultWindowSize() {
      String plainValue = this.getDefault("minecraft.size");
      return IntegerArray.parseIntegerArray(plainValue).toArray();
   }

   public VersionFilter getVersionFilter() {
      VersionFilter filter = new VersionFilter();
      ReleaseType[] var5;
      int var4 = (var5 = ReleaseType.getDefinable()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         ReleaseType type = var5[var3];
         if (!type.equals(ReleaseType.UNKNOWN)) {
            boolean include = this.getBoolean("minecraft.versions." + type);
            if (!include) {
               filter.excludeType(type);
            }
         }
      }

      return filter;
   }

   public String getDefault(String key) {
      return getStringOf(this.defaults.get(key));
   }

   public int getDefaultInteger(String key) {
      return getIntegerOf(this.defaults.get(key), 0);
   }

   public double getDefaultDouble(String key) {
      return getDoubleOf(this.defaults.get(key), 0.0D);
   }

   public float getDefaultFloat(String key) {
      return getFloatOf(this.defaults.get(key), 0.0F);
   }

   public long getDefaultLong(String key) {
      return getLongOf(this.defaults.get(key), 0L);
   }

   public boolean getDefaultBoolean(String key) {
      return getBooleanOf(this.defaults.get(key), false);
   }

   public void set(String key, Object value, boolean flush) {
      if (!this.constants.containsKey(key)) {
         super.set(key, value, flush);
      }
   }

   public void save() throws IOException {
      if (!this.isSaveable()) {
         throw new UnsupportedOperationException();
      } else {
         Properties temp = copyProperties(this.properties);
         Iterator var3 = this.constants.keySet().iterator();

         while(var3.hasNext()) {
            String key = (String)var3.next();
            temp.remove(key);
         }

         File file = (File)this.input;
         temp.store(new FileOutputStream(file), this.comments);
      }
   }

   public File getFile() {
      return !this.isSaveable() ? null : (File)this.input;
   }

   private List getSupportedLocales() {
      this.log(new Object[]{"Searching for supported locales..."});
      Pattern lang_pattern = Pattern.compile("lang/([\\w]+)\\.ini");
      File file = FileUtil.getRunningJar();
      ArrayList locales = new ArrayList();

      try {
         URL jar = file.toURI().toURL();
         ZipInputStream zip = new ZipInputStream(jar.openStream());

         while(true) {
            ZipEntry e = zip.getNextEntry();
            if (e == null) {
               return (List)(locales.isEmpty() ? this.defaultLocales : locales);
            }

            String name = e.getName();
            Matcher mt = lang_pattern.matcher(name);
            if (mt.matches()) {
               this.log(new Object[]{"Found locale:", mt.group(1)});
               locales.add(getLocaleOf(mt.group(1)));
            }
         }
      } catch (Exception var9) {
         this.log(new Object[]{"Cannot get locales!", var9});
         return this.defaultLocales;
      }
   }

   private List getDefaultLocales() {
      List l = new ArrayList();
      l.add(getLocaleOf("en_US"));
      l.add(getLocaleOf("ru_RU"));
      l.add(getLocaleOf("uk_UA"));
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

   public static enum ActionOnLaunch {
      HIDE,
      EXIT;

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

   public static enum ConnectionQuality {
      GOOD(2, 5, 8, 15000),
      NORMAL(5, 10, 4, 45000),
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

      public int[] getConfiguration() {
         return this.configuration;
      }

      public int getMinTries() {
         return this.minTries;
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

      public String toString() {
         return super.toString().toLowerCase();
      }

      public static Configuration.ConsoleType getDefault() {
         return NONE;
      }
   }
}
