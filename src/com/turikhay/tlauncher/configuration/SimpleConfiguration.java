package com.turikhay.tlauncher.configuration;

import com.turikhay.util.StringUtil;
import com.turikhay.util.U;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public class SimpleConfiguration implements AbstractConfiguration {
   final Properties properties;
   Object input;
   String comments;

   SimpleConfiguration() {
      this.properties = new Properties();
   }

   public SimpleConfiguration(InputStream stream) throws IOException {
      this();
      loadFromStream(this.properties, stream);
      this.input = stream;
   }

   SimpleConfiguration(File file) {
      this();

      try {
         loadFromFile(this.properties, file);
      } catch (Exception var3) {
         this.log("Error loading config from file:", var3);
      }

      this.input = file;
   }

   SimpleConfiguration(URL url) throws IOException {
      this();
      loadFromURL(this.properties, url);
      this.input = url;
   }

   public String get(String key) {
      return this.getStringOf(this.properties.getProperty(key));
   }

   protected String getStringOf(Object obj) {
      String s;
      if (obj == null) {
         s = null;
      } else {
         s = obj.toString();
         if (s.isEmpty()) {
            s = null;
         }
      }

      return s;
   }

   void set(String key, Object value, boolean flush) {
      if (key == null) {
         throw new NullPointerException();
      } else {
         if (value == null) {
            this.properties.remove(key);
         } else {
            this.properties.setProperty(key, value.toString());
         }

         if (flush && this.isSaveable()) {
            this.store();
         }

      }
   }

   public void set(String key, Object value) {
      this.set(key, value, true);
   }

   void set(Map map, boolean flush) {
      Iterator var4 = map.entrySet().iterator();

      while(var4.hasNext()) {
         Entry en = (Entry)var4.next();
         String key = (String)en.getKey();
         Object value = en.getValue();
         if (value == null) {
            this.properties.remove(key);
         } else {
            this.properties.setProperty(key, value.toString());
         }
      }

      if (flush && this.isSaveable()) {
         this.store();
      }

   }

   public void set(Map map) {
      this.set(map, false);
   }

   public Set getKeys() {
      Set set = new HashSet();
      Iterator var3 = this.properties.keySet().iterator();

      while(var3.hasNext()) {
         Object obj = var3.next();
         set.add(this.getStringOf(obj));
      }

      return Collections.unmodifiableSet(set);
   }

   public String getDefault(String key) {
      return null;
   }

   public int getInteger(String key, int def) {
      return this.getIntegerOf(this.get(key), 0);
   }

   public int getInteger(String key) {
      return this.getInteger(key, 0);
   }

   protected int getIntegerOf(Object obj, int def) {
      try {
         return Integer.parseInt(obj.toString());
      } catch (Exception var4) {
         return def;
      }
   }

   public double getDouble(String key) {
      return this.getDoubleOf(this.get(key), 0.0D);
   }

   protected double getDoubleOf(Object obj, double def) {
      try {
         return Double.parseDouble(obj.toString());
      } catch (Exception var5) {
         return def;
      }
   }

   public float getFloat(String key) {
      return this.getFloatOf(this.get(key), 0.0F);
   }

   protected float getFloatOf(Object obj, float def) {
      try {
         return Float.parseFloat(obj.toString());
      } catch (Exception var4) {
         return def;
      }
   }

   public long getLong(String key) {
      return this.getLongOf(this.get(key), 0L);
   }

   protected long getLongOf(Object obj, long def) {
      try {
         return Long.parseLong(obj.toString());
      } catch (Exception var5) {
         return def;
      }
   }

   public boolean getBoolean(String key) {
      return this.getBooleanOf(this.get(key), false);
   }

   protected boolean getBooleanOf(Object obj, boolean def) {
      try {
         return StringUtil.parseBoolean(obj.toString());
      } catch (Exception var4) {
         return def;
      }
   }

   public int getDefaultInteger(String key) {
      return 0;
   }

   public double getDefaultDouble(String key) {
      return 0.0D;
   }

   public float getDefaultFloat(String key) {
      return 0.0F;
   }

   public long getDefaultLong(String key) {
      return 0L;
   }

   public boolean getDefaultBoolean(String key) {
      return false;
   }

   public void save() throws IOException {
      if (!this.isSaveable()) {
         throw new UnsupportedOperationException();
      } else {
         File file = (File)this.input;
         this.properties.store(new FileOutputStream(file), this.comments);
      }
   }

   public void store() {
      try {
         this.save();
      } catch (IOException var2) {
         this.log("Cannot store values!", var2);
      }

   }

   public void clear() {
      this.properties.clear();
   }

   public boolean isSaveable() {
      return this.input != null && this.input instanceof File;
   }

   private static void loadFromStream(Properties properties, InputStream stream) throws IOException {
      if (stream == null) {
         throw new NullPointerException();
      } else {
         Reader reader = new InputStreamReader(new BufferedInputStream(stream), Charset.forName("UTF-8"));
         properties.clear();
         properties.load(reader);
      }
   }

   static Properties loadFromStream(InputStream stream) throws IOException {
      Properties properties = new Properties();
      loadFromStream(properties, stream);
      return properties;
   }

   private static void loadFromFile(Properties properties, File file) throws IOException {
      if (file == null) {
         throw new NullPointerException();
      } else {
         FileInputStream stream = new FileInputStream(file);
         loadFromStream(properties, stream);
      }
   }

   protected static Properties loadFromFile(File file) throws IOException {
      Properties properties = new Properties();
      loadFromFile(properties, file);
      return properties;
   }

   private static void loadFromURL(Properties properties, URL url) throws IOException {
      if (url == null) {
         throw new NullPointerException();
      } else {
         InputStream connection = url.openStream();
         loadFromStream(properties, connection);
      }
   }

   protected static Properties loadFromURL(URL url) throws IOException {
      Properties properties = new Properties();
      loadFromURL(properties, url);
      return properties;
   }

   static void copyProperties(Properties src, Properties dest, boolean wipe) {
      if (src == null) {
         throw new NullPointerException("src is NULL");
      } else if (dest == null) {
         throw new NullPointerException("dest is NULL");
      } else {
         if (wipe) {
            dest.clear();
         }

         Iterator var4 = src.entrySet().iterator();

         while(var4.hasNext()) {
            Entry en = (Entry)var4.next();
            String key = en.getKey() == null ? null : en.getKey().toString();
            String value = en.getKey() == null ? null : en.getValue().toString();
            dest.setProperty(key, value);
         }

      }
   }

   static Properties copyProperties(Properties src) {
      Properties properties = new Properties();
      copyProperties(src, properties, false);
      return properties;
   }

   void log(Object... o) {
      U.log("[" + this.getClass().getSimpleName() + "]", o);
   }
}
