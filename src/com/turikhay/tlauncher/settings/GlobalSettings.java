package com.turikhay.tlauncher.settings;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.util.FileUtil;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import joptsimple.OptionSet;

public class GlobalSettings extends Settings {
   private static final Pattern lang_pattern = Pattern.compile("lang/([\\w]+)\\.ini");
   public static final Locale DEFAULT_LOCALE;
   public static final List DEFAULT_LOCALES;
   public static final List SUPPORTED_LOCALE;
   private File file;
   private boolean saveable;
   private static boolean firstRun;
   private GlobalDefaults d;
   private Map cs = new HashMap();
   double version = 0.14D;

   static {
      DEFAULT_LOCALE = Locale.US;
      DEFAULT_LOCALES = getDefaultLocales();
      SUPPORTED_LOCALE = getSupportedLocales();
   }

   public static GlobalSettings createInstance(OptionSet set) throws IOException {
      Object path = set != null ? set.valueOf("settings") : null;
      if (path == null) {
         URL resource = GlobalSettings.class.getResource("/settings.ini");
         if (resource != null) {
            return new GlobalSettings(resource, set);
         }
      }

      File file = path == null ? getDefaultFile() : new File(path.toString());
      if (!file.exists()) {
         firstRun = true;
      }

      return new GlobalSettings(file, set);
   }

   private GlobalSettings(URL url, OptionSet set) throws IOException {
      super(url);
      U.log("Settings URL:", url);
      this.init(set);
   }

   private GlobalSettings(File file, OptionSet set) throws IOException {
      super(file);
      U.log("Settings file:", file);
      this.init(set);
   }

   private void init(OptionSet set) throws IOException {
      this.d = new GlobalDefaults(this);
      this.cs = ArgumentParser.parse(set);
      boolean forcedrepair = this.getDouble("settings.version") != this.version;
      this.saveable = this.input instanceof File;
      Iterator var4 = this.d.getMap().entrySet().iterator();

      while(var4.hasNext()) {
         Entry curen = (Entry)var4.next();
         String key = (String)curen.getKey();
         String value = (String)this.s.get(key);
         Object defvalue = this.d.get(key);
         if (defvalue != null) {
            try {
               if (forcedrepair) {
                  throw new Exception();
               }

               if (defvalue instanceof Integer) {
                  Integer.parseInt(value);
               } else if (defvalue instanceof Boolean) {
                  this.parseBoolean(value);
               } else if (defvalue instanceof Double) {
                  Double.parseDouble(value);
               } else if (defvalue instanceof Long) {
                  Long.parseLong(value);
               } else if (defvalue instanceof GlobalSettings.ActionOnLaunch) {
                  this.parseLaunchAction(value);
               } else if (defvalue instanceof IntegerArray) {
                  IntegerArray.parseIntegerArray(value);
               }
            } catch (Exception var9) {
               this.repair(key, defvalue, !this.saveable);
               value = defvalue.toString();
            }

            if (!this.saveable) {
               this.cs.put(key, value);
            }
         }
      }

      if (this.saveable) {
         this.save();
      }

   }

   public boolean isFirstRun() {
      return firstRun;
   }

   public String get(String key) {
      Object r = this.cs.containsKey(key) ? this.cs.get(key) : this.s.get(key);
      return r != null && !r.equals("") ? r.toString() : null;
   }

   public void set(String key, Object value, boolean save) {
      if (!this.cs.containsKey(key)) {
         super.set(key, value, save);
      } else {
         if (value == null) {
            value = "";
         }

         this.cs.remove(key);
         this.cs.put(key, value.toString());
         if (save) {
            try {
               this.save();
            } catch (IOException var5) {
               throw new SettingsException(this, "Cannot save set value!", var5);
            }
         }

      }
   }

   public boolean isSaveable(String key) {
      return !this.cs.containsKey(key);
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public String getDefault(String key) {
      String r = "" + this.d.get(key);
      return r == "" ? null : r;
   }

   public int getDefaultInteger(String key) {
      try {
         return Integer.parseInt("" + this.d.get(key));
      } catch (Exception var3) {
         return 0;
      }
   }

   public int getInteger(String key, int min, int max) {
      int i = this.getInteger(key);
      return i >= min && i <= max ? i : this.getDefaultInteger(key);
   }

   public int getInteger(String key, int min) {
      return this.getInteger(key, min, Integer.MAX_VALUE);
   }

   public long getDefaultLong(String key) {
      try {
         return Long.parseLong("" + this.d.get(key));
      } catch (Exception var3) {
         return 0L;
      }
   }

   public double getDefaultDouble(String key) {
      try {
         return Double.parseDouble("" + this.d.get(key));
      } catch (Exception var3) {
         return 0.0D;
      }
   }

   public float getDefaultFloat(String key) {
      try {
         return Float.parseFloat("" + this.d.get(key));
      } catch (Exception var3) {
         return 0.0F;
      }
   }

   public boolean getDefaultBoolean(String key) {
      try {
         return Boolean.parseBoolean("" + this.d.get(key));
      } catch (Exception var3) {
         return false;
      }
   }

   public Locale getLocale() {
      String locale = this.get("locale");
      Locale[] var5;
      int var4 = (var5 = Locale.getAvailableLocales()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         Locale lookup = var5[var3];
         String lookup_name = lookup.toString();
         Iterator var8 = SUPPORTED_LOCALE.iterator();

         while(var8.hasNext()) {
            String curloc = (String)var8.next();
            if (lookup_name.equals(curloc) && curloc.equals(locale)) {
               return lookup;
            }
         }
      }

      return DEFAULT_LOCALE;
   }

   public static Locale getSupported() {
      Locale using = Locale.getDefault();
      String using_name = using.toString();
      Iterator var3 = SUPPORTED_LOCALE.iterator();

      while(var3.hasNext()) {
         String supported = (String)var3.next();
         if (supported.equals(using_name)) {
            return using;
         }
      }

      return Locale.US;
   }

   public GlobalSettings.ActionOnLaunch getActionOnLaunch() {
      String action = this.get("minecraft.onlaunch");
      GlobalSettings.ActionOnLaunch get = GlobalSettings.ActionOnLaunch.get(action);
      return get != null ? get : GlobalSettings.ActionOnLaunch.getDefault();
   }

   private boolean parseBoolean(String b) throws Exception {
      if (b.equalsIgnoreCase("true")) {
         return true;
      } else if (b.equalsIgnoreCase("false")) {
         return false;
      } else {
         throw new Exception();
      }
   }

   private void parseLaunchAction(String b) throws Exception {
      if (!GlobalSettings.ActionOnLaunch.parse(b)) {
         throw new Exception();
      }
   }

   private void repair(String key, Object value, boolean unsaveable) throws IOException {
      U.log("Field \"" + key + "\" in GlobalSettings is invalid.");
      this.set(key, value.toString(), false);
      if (unsaveable) {
         this.cs.put(key, value);
      }

   }

   public static File getDefaultFile() {
      return MinecraftUtil.getSystemRelatedFile(TLauncher.getSettingsFile());
   }

   public File getFile() {
      return this.file == null ? getDefaultFile() : this.file;
   }

   public void setFile(File f) {
      if (f == null) {
         throw new IllegalArgumentException("File cannot be NULL!");
      } else {
         U.log("Set settings file: " + f.toString());
         this.file = f;
      }
   }

   public void save() throws IOException {
      if (this.input instanceof File) {
         File file = (File)this.input;
         StringBuilder r = new StringBuilder();
         boolean first = true;

         String key;
         Object value;
         for(Iterator var5 = this.s.entrySet().iterator(); var5.hasNext(); r.append(key + this.DELIMITER_CHAR + value.toString().replace(this.NEWLINE_CHAR, "\\" + this.NEWLINE_CHAR))) {
            Entry curen = (Entry)var5.next();
            key = (String)curen.getKey();
            value = curen.getValue();
            if (value == null) {
               value = "";
            }

            if (!first) {
               r.append(this.NEWLINE_CHAR);
            } else {
               first = false;
            }
         }

         String towrite = r.toString();
         FileOutputStream os = new FileOutputStream(file);
         OutputStreamWriter ow = new OutputStreamWriter(os, "UTF-8");
         ow.write(towrite);
         ow.close();
      }
   }

   public int[] getWindowSize() {
      int[] d_sizes = new int[]{925, 530};

      int[] w_sizes;
      try {
         w_sizes = IntegerArray.parseIntegerArray(this.get("minecraft.size")).toArray();
      } catch (Exception var4) {
         w_sizes = d_sizes;
      }

      if (w_sizes[0] < d_sizes[0] || w_sizes[1] < d_sizes[1]) {
         w_sizes = d_sizes;
      }

      return w_sizes;
   }

   private static List getSupportedLocales() {
      File file = FileUtil.getRunningJar();
      ArrayList locales = new ArrayList();

      try {
         URL jar = file.toURI().toURL();
         ZipInputStream zip = new ZipInputStream(jar.openStream());

         while(true) {
            ZipEntry e = zip.getNextEntry();
            if (e == null) {
               return (List)(locales.isEmpty() ? DEFAULT_LOCALES : locales);
            }

            String name = e.getName();
            if (name.startsWith("lang/")) {
               Matcher mt = lang_pattern.matcher(name);
               if (mt.matches()) {
                  U.log("Found locale:", mt.group());
                  locales.add(mt.group(1));
               }
            }
         }
      } catch (Exception var7) {
         U.log("Cannot get locales!", var7);
         return DEFAULT_LOCALES;
      }
   }

   private static List getDefaultLocales() {
      List l = new ArrayList();
      l.add("en_US");
      l.add("ru_RU");
      l.add("uk_UA");
      return l;
   }

   public static enum ActionOnLaunch {
      HIDE,
      EXIT;

      public static boolean parse(String val) {
         GlobalSettings.ActionOnLaunch[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            GlobalSettings.ActionOnLaunch cur = var4[var2];
            if (cur.toString().toLowerCase().equals(val)) {
               return true;
            }
         }

         return false;
      }

      public static GlobalSettings.ActionOnLaunch get(String val) {
         GlobalSettings.ActionOnLaunch[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            GlobalSettings.ActionOnLaunch cur = var4[var2];
            if (cur.toString().toLowerCase().equals(val)) {
               return cur;
            }
         }

         return null;
      }

      public String toString() {
         return super.toString().toLowerCase();
      }

      public static GlobalSettings.ActionOnLaunch getDefault() {
         return HIDE;
      }
   }
}
