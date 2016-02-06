package ru.turikhay.tlauncher.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

public class LangConfiguration extends SimpleConfiguration {
   private final Locale[] locales;
   private final Properties[] prop;
   private final SortedMap[] numericPatterns;
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
         this.numericPatterns = (TreeMap[])((TreeMap[])Array.newInstance(TreeMap.class, size));

         int var8;
         Iterator var10;
         Object var9;
         for(var8 = 0; var8 < size; ++var8) {
            if (locales[var8].toString().equals("ru_RU")) {
               defLocale = var8;
            }

            this.numericPatterns[var8] = new TreeMap();
            var10 = this.prop[var8].keySet().iterator();

            while(var10.hasNext()) {
               var9 = var10.next();
               if (var9 instanceof String) {
                  String key = (String)var9;
                  if (key.startsWith("numeric.")) {
                     U.debug(locales[var8], "found numeric:", key);
                     this.numericPatterns[var8].put(key, Pattern.compile(this.prop[var8].getProperty(key)));
                  }
               }
            }
         }

         U.debug(this.numericPatterns);
         this.setSelected(select);
         if (TLauncher.getDebug() && defLocale != -1) {
            var10 = this.prop[defLocale].keySet().iterator();

            label124:
            while(true) {
               boolean isNumeric;
               do {
                  if (!var10.hasNext()) {
                     for(var8 = 0; var8 < size; ++var8) {
                        if (var8 != defLocale) {
                           Iterator var13 = this.prop[var8].keySet().iterator();

                           while(var13.hasNext()) {
                              Object var11 = var13.next();
                              boolean isNumeric = false;
                              if (var11 instanceof String) {
                                 String key = (String)var11;
                                 Iterator i$ = this.numericPatterns[var8].keySet().iterator();

                                 while(i$.hasNext()) {
                                    String numericKey = (String)i$.next();
                                    if (key.endsWith('.' + numericKey)) {
                                       isNumeric = true;
                                       break;
                                    }
                                 }
                              }

                              if (!isNumeric && !this.prop[defLocale].containsKey(var11)) {
                                 U.log("Locale", locales[var8], "contains redundant key", var11);
                              }
                           }
                        }
                     }
                     break label124;
                  }

                  var9 = var10.next();
                  isNumeric = false;
                  if (var9 instanceof String) {
                     String key = (String)var9;
                     Iterator i$ = this.numericPatterns[defLocale].keySet().iterator();

                     while(i$.hasNext()) {
                        String numericKey = (String)i$.next();
                        if (key.endsWith('.' + numericKey)) {
                           isNumeric = true;
                           break;
                        }
                     }
                  }
               } while(isNumeric);

               for(int var12 = 0; var12 < size; ++var12) {
                  if (var12 != defLocale && !this.prop[defLocale].containsKey(var9)) {
                     U.log("Locale", locales[var12], "doesn't contain key", var9);
                  }
               }
            }
         }

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
         boolean found = false;

         for(int i = 0; i < this.locales.length; ++i) {
            if (this.locales[i].equals(select)) {
               this.i = i;
               found = true;
               break;
            }
         }

         if (!found) {
            throw new IllegalArgumentException("Cannot find Locale:" + select);
         }
      }
   }

   public String nget(String key) {
      return key == null ? null : this.prop[this.i].getProperty(key);
   }

   public String get(String key) {
      String value = this.nget(key);
      return value == null ? key : value;
   }

   public String nget(String key, Object... vars) {
      String value;
      label41: {
         value = this.nget(key);
         if (key == null) {
            if (value != null) {
               break label41;
            }
         } else if (!key.equals(value)) {
            break label41;
         }

         return null;
      }

      String[] variables = checkVariables(vars);

      for(int i = 0; i < variables.length; ++i) {
         boolean patternFound = false;
         Iterator i$ = this.numericPatterns[this.i].keySet().iterator();

         while(i$.hasNext()) {
            String patternKey = (String)i$.next();
            String replacement = this.nget(key + '.' + patternKey);
            if (replacement != null) {
               patternFound = true;
               Pattern pattern = (Pattern)this.numericPatterns[this.i].get(patternKey);
               this.log(new Object[]{key, patternKey, variables[i], pattern.matcher(variables[i]).matches()});
               if (pattern.matcher(variables[i]).matches()) {
                  value = StringUtils.replace(value, "%" + i, StringUtils.replace(replacement, "%n", variables[i]));
                  break;
               }
            }
         }

         if (!patternFound) {
            value = StringUtils.replace(value, "%" + i, variables[i]);
         }
      }

      return value;
   }

   public String get(String key, Object... vars) {
      String value = this.nget(key, vars);
      return value == null ? key : value;
   }

   public void set(String key, Object value) {
      throw new UnsupportedOperationException();
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
