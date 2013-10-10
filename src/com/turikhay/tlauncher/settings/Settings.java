package com.turikhay.tlauncher.settings;

import com.turikhay.tlauncher.util.FileUtil;
import com.turikhay.tlauncher.util.U;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Settings {
   final Settings.InputType inputType;
   final String filename;
   public static final char DEFAULT_DELIMITER_CHAR = '=';
   public static final char DEFAULT_NEWLINE_CHAR = '\n';
   public static final char DEFAULT_COMMENT_CHAR = '#';
   public static final String DEFAULT_CHARSET = "UTF-8";
   protected final String CHARSET;
   protected final String DELIMITER_CHAR;
   protected final String NEWLINE_CHAR;
   protected final String COMMENT_CHAR;
   final Object input;
   protected Map s;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$settings$Settings$InputType;

   public Settings(String charset, char delimiter, char newline, char comment_char) {
      this.s = new HashMap();
      this.input = this.filename = null;
      this.inputType = null;
      this.CHARSET = charset;
      this.DELIMITER_CHAR = String.valueOf(delimiter);
      this.NEWLINE_CHAR = String.valueOf(newline);
      this.COMMENT_CHAR = String.valueOf(comment_char);
   }

   public Settings() {
      this("UTF-8", '=', '\n', '#');
   }

   public Settings(URL resource, String charset, char delimiter, char newline, char comment_char) throws IOException {
      this.s = new HashMap();
      if (resource == null) {
         throw new SettingsException("Given resourse is NULL!");
      } else {
         this.input = resource;
         this.filename = resource.getFile();
         this.inputType = Settings.InputType.STREAM;
         this.CHARSET = charset;
         this.DELIMITER_CHAR = String.valueOf(delimiter);
         this.NEWLINE_CHAR = String.valueOf(newline);
         this.COMMENT_CHAR = String.valueOf(comment_char);
         InputStream stream = resource.openStream();
         this.readFromStream(stream);
      }
   }

   public Settings(URL resource) throws IOException {
      this(resource, "UTF-8", '=', '\n', '#');
   }

   public Settings(InputStream stream, String charset, char delimiter, char newline, char comment_char) throws IOException {
      this.s = new HashMap();
      if (stream == null) {
         throw new SettingsException("Given stream is NULL!");
      } else {
         this.input = stream;
         this.filename = null;
         this.inputType = Settings.InputType.STREAM;
         this.CHARSET = charset;
         this.DELIMITER_CHAR = String.valueOf(delimiter);
         this.NEWLINE_CHAR = String.valueOf(newline);
         this.COMMENT_CHAR = String.valueOf(comment_char);
         this.readFromStream(stream);
      }
   }

   public Settings(InputStream stream) throws IOException {
      this(stream, "UTF-8", '=', '\n', '#');
   }

   public Settings(File file, String charset, char delimiter, char newline, char comment_char) throws IOException {
      this.s = new HashMap();
      if (file == null) {
         throw new SettingsException("Given file is NULL!");
      } else {
         this.input = file;
         this.filename = file.getName();
         this.inputType = Settings.InputType.FILE;
         this.CHARSET = charset;
         this.DELIMITER_CHAR = String.valueOf(delimiter);
         this.NEWLINE_CHAR = String.valueOf(newline);
         this.COMMENT_CHAR = String.valueOf(comment_char);
         FileUtil.createFile(file);
         InputStream is = new FileInputStream(file);
         this.readFromStream(is);
      }
   }

   public Settings(File file) throws IOException {
      this(file, "UTF-8", '=', '\n', '#');
   }

   private void readFromStream(InputStream stream) throws IOException {
      if (stream == null) {
         throw new SettingsException(this, "Given stream is NULL!");
      } else if (stream.available() != 0) {
         InputStreamReader reader = new InputStreamReader(stream, this.CHARSET);

         String b;
         for(b = ""; reader.ready(); b = b + (char)reader.read()) {
         }

         reader.close();
         this.readFromString(b);
      }
   }

   private void readFromString(String string) {
      if (string == null) {
         throw new SettingsException(this, "Given string is NULL!");
      } else if (string.length() != 0) {
         String[] lines = string.split(this.NEWLINE_CHAR);

         for(int x = 0; x < lines.length; ++x) {
            String line = lines[x];
            if (!line.startsWith(this.COMMENT_CHAR)) {
               String[] sline = line.split(this.DELIMITER_CHAR);
               String curkey = sline[0];
               if (curkey != "") {
                  String curvalue = "";

                  for(int y = 1; y < sline.length; ++y) {
                     curvalue = curvalue + this.DELIMITER_CHAR + sline[y];
                  }

                  if (curvalue != "") {
                     curvalue = curvalue.substring(1).replace("\\n", this.NEWLINE_CHAR);
                  }

                  this.s.put(curkey, curvalue);
               }
            }
         }

      }
   }

   public String get(String key) {
      String r = (String)this.s.get(key);
      return r != "" && r != null ? r : key;
   }

   public String nget(String key) {
      return (String)this.s.get(key);
   }

   public String getRand(String key) {
      String r = this.get(key);
      if (r == "") {
         return key;
      } else {
         String[] v = r.split("\\|");
         return v[U.random(0, v.length)];
      }
   }

   public String get(String key, String replace, Object with) {
      String r = this.get(key);
      return r == null ? key : r.replace("%" + replace, "" + with);
   }

   public String get(String key, String replace0, Object with0, String replace1, Object with1) {
      String r = this.get(key);
      return r == null ? key : r.replace("%" + replace0, "" + with0).replace("%" + replace1, "" + with1);
   }

   public int getInteger(String key) {
      try {
         return Integer.parseInt(this.get(key));
      } catch (Exception var3) {
         return 0;
      }
   }

   public int getInteger(String key, int min, int def) {
      int i = this.getInteger(key);
      return i < min ? def : i;
   }

   public long getLong(String key) {
      try {
         return Long.parseLong(this.get(key));
      } catch (Exception var3) {
         return 0L;
      }
   }

   public double getDouble(String key) {
      try {
         return Double.parseDouble(this.get(key));
      } catch (Exception var3) {
         return 0.0D;
      }
   }

   public float getFloat(String key) {
      try {
         return Float.parseFloat(this.get(key));
      } catch (Exception var3) {
         return 0.0F;
      }
   }

   public boolean getBoolean(String key) {
      try {
         return Boolean.parseBoolean(this.get(key));
      } catch (Exception var3) {
         return false;
      }
   }

   public String[] getAll() {
      int size = this.s.size();
      int x = -1;
      String[] r = new String[size];

      Entry curen;
      for(Iterator var5 = this.s.entrySet().iterator(); var5.hasNext(); r[x] = (String)curen.getKey() + " " + this.DELIMITER_CHAR + " \"" + (String)curen.getValue() + "\"") {
         curen = (Entry)var5.next();
         ++x;
      }

      return r;
   }

   public void set(String key, Object value, boolean save) {
      if (this.s.containsKey(key)) {
         this.s.remove(key);
      }

      if (value == null) {
         value = "";
      }

      this.s.put(key, value.toString());
      if (save) {
         try {
            this.save();
         } catch (IOException var5) {
            throw new SettingsException(this, "Cannot save set value!", var5);
         }
      }

   }

   public void set(Map map, boolean save) {
      Iterator var4 = map.entrySet().iterator();

      while(var4.hasNext()) {
         Entry curen = (Entry)var4.next();
         this.set((String)curen.getKey(), curen.getValue(), false);
      }

      if (save) {
         try {
            this.save();
         } catch (IOException var5) {
            throw new SettingsException(this, "Cannot save map!", var5);
         }
      }

   }

   public void set(String key, Object value) {
      this.set(key, value, true);
   }

   public boolean has(String key) {
      return this.s.containsKey(key);
   }

   public String createString() {
      StringBuilder r = new StringBuilder();
      boolean first = true;

      Entry curen;
      for(Iterator var4 = this.s.entrySet().iterator(); var4.hasNext(); r.append((String)curen.getKey() + this.DELIMITER_CHAR + ((String)curen.getValue()).replace(this.NEWLINE_CHAR, "\\" + this.NEWLINE_CHAR))) {
         curen = (Entry)var4.next();
         if (!first) {
            r.append(this.NEWLINE_CHAR);
         } else {
            first = false;
         }
      }

      return r.toString();
   }

   public void save() throws IOException {
      switch($SWITCH_TABLE$com$turikhay$tlauncher$settings$Settings$InputType()[this.inputType.ordinal()]) {
      case 1:
         File file = (File)this.input;
         String towrite = this.createString();
         FileOutputStream os = new FileOutputStream(file);
         OutputStreamWriter ow = new OutputStreamWriter(os, "UTF-8");
         ow.write(towrite);
         ow.close();
      default:
         return;
      case 2:
         throw new SettingsException("Cannot write to input stream!");
      }
   }

   public void reload(InputStream stream) throws IOException {
      this.s.clear();
      this.readFromStream(stream);
   }

   public void reload(File file) throws FileNotFoundException, IOException {
      this.s.clear();
      this.readFromStream(new FileInputStream(file));
   }

   public void reload(URL url) throws IOException {
      this.s.clear();
      this.readFromStream(url.openStream());
   }

   public void reload(String str) {
      this.s.clear();
      this.readFromString(str);
   }

   public boolean canBeSaved() {
      switch($SWITCH_TABLE$com$turikhay$tlauncher$settings$Settings$InputType()[this.inputType.ordinal()]) {
      case 1:
         return true;
      case 2:
      default:
         return false;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$settings$Settings$InputType() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$settings$Settings$InputType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Settings.InputType.values().length];

         try {
            var0[Settings.InputType.FILE.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Settings.InputType.STREAM.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$settings$Settings$InputType = var0;
         return var0;
      }
   }

   private static enum InputType {
      FILE,
      STREAM;
   }
}
