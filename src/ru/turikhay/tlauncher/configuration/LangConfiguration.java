package ru.turikhay.tlauncher.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

public class LangConfiguration extends SimpleConfiguration {
   private final Locale[] locales;
   private final Properties[] prop;
   private int i;

   public LangConfiguration(Locale[] locales, Locale select) throws IOException {
      if (locales == null) {
         throw new NullPointerException();
      } else {
         int size = locales.length;
         this.locales = locales;
         this.prop = new Properties[size];

         int defLocale;
         for(defLocale = 0; defLocale < size; ++defLocale) {
            Locale locale = locales[defLocale];
            if (locale == null) {
               throw new NullPointerException("Locale at #" + defLocale + " is NULL!");
            }

            String localeName = locale.toString();
            InputStream stream = this.getClass().getResourceAsStream("/lang/" + localeName);
            if (stream == null) {
               throw new IOException("Cannot find locale file for: " + localeName);
            }

            this.prop[defLocale] = loadFromStream(stream);
            if (localeName.equals("en_US")) {
               copyProperties(this.prop[defLocale], this.properties, true);
            }
         }

         defLocale = -1;

         int i;
         for(i = 0; i < size; ++i) {
            if (locales[i].toString().equals("ru_RU")) {
               defLocale = i;
               break;
            }
         }

         if (TLauncher.getDebug() && defLocale != -1) {
            Iterator var10 = this.prop[defLocale].keySet().iterator();

            while(var10.hasNext()) {
               Object key = var10.next();

               for(int i = 0; i < size; ++i) {
                  if (i != defLocale && !this.prop[i].containsKey(key)) {
                     U.log("Locale", locales[i], "doesn't contain key", key);
                  }
               }
            }

            for(i = 0; i < size; ++i) {
               if (i != defLocale) {
                  Iterator var13 = this.prop[i].keySet().iterator();

                  while(var13.hasNext()) {
                     Object key = var13.next();
                     if (!this.prop[defLocale].containsKey(key)) {
                        U.log("Locale", locales[i], "contains redundant key", key);
                     }
                  }
               }
            }
         }

         this.setSelected(select);
      }
   }

   public Locale[] getLocales() {
      return this.locales;
   }

   public Locale getSelected() {
      return this.locales[this.i];
   }

   public void setSelected(Locale select) {
      if (select == null) {
         throw new NullPointerException();
      } else {
         for(int i = 0; i < this.locales.length; ++i) {
            if (this.locales[i].equals(select)) {
               this.i = i;
               return;
            }
         }

         throw new IllegalArgumentException("Cannot find Locale:" + select);
      }
   }

   public String nget(String key) {
      if (key == null) {
         return null;
      } else {
         String value = this.prop[this.i].getProperty(key);
         return value == null ? this.getDefault(key) : value;
      }
   }

   public String get(String key) {
      String value = this.nget(key);
      return value == null ? key : value;
   }

   public String nget(String key, Object... vars) {
      String value = this.get(key);
      if (value == null) {
         return null;
      } else {
         String[] variables = checkVariables(vars);

         for(int i = 0; i < variables.length; ++i) {
            value = value.replace("%" + i, variables[i]);
         }

         return value;
      }
   }

   public String get(String key, Object... vars) {
      String value = this.nget(key, vars);
      return value == null ? key : value;
   }

   public void set(String key, Object value) {
      throw new UnsupportedOperationException();
   }

   public String getDefault(String key) {
      return super.get(key);
   }

   private static String[] checkVariables(Object[] check) {
      if (check == null) {
         throw new NullPointerException();
      } else if (check.length == 1 && check[0] == null) {
         return new String[0];
      } else {
         String[] string = new String[check.length];

         for(int i = 0; i < check.length; ++i) {
            if (check[i] == null) {
               throw new NullPointerException("Variable at index " + i + " is NULL!");
            }

            string[i] = check[i].toString();
         }

         return string;
      }
   }
}
