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
            Locale i = locales[defLocale];
            if (i == null) {
               throw new NullPointerException("Locale at #" + defLocale + " is NULL!");
            }

            String key = i.toString();
            InputStream i1 = this.getClass().getResourceAsStream("/lang/" + key);
            if (i1 == null) {
               throw new IOException("Cannot find locale file for: " + key);
            }

            this.prop[defLocale] = loadFromStream(i1);
            if (key.equals("en_US")) {
               copyProperties(this.prop[defLocale], this.properties, true);
            }
         }

         defLocale = -1;

         int var8;
         for(var8 = 0; var8 < size; ++var8) {
            if (locales[var8].toString().equals("ru_RU")) {
               defLocale = var8;
               break;
            }
         }

         if (TLauncher.getDebug() && defLocale != -1) {
            Iterator var10 = this.prop[defLocale].keySet().iterator();

            while(var10.hasNext()) {
               Object var9 = var10.next();

               for(int var12 = 0; var12 < size; ++var12) {
                  if (var12 != defLocale && !this.prop[var12].containsKey(var9)) {
                     U.log("Locale", locales[var12], "doesn't contain key", var9);
                  }
               }
            }

            for(var8 = 0; var8 < size; ++var8) {
               if (var8 != defLocale) {
                  Iterator var13 = this.prop[var8].keySet().iterator();

                  while(var13.hasNext()) {
                     Object var11 = var13.next();
                     if (!this.prop[defLocale].containsKey(var11)) {
                        U.log("Locale", locales[var8], "contains redundant key", var11);
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
