package ru.turikhay.tlauncher.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

public class LangConfiguration extends SimpleConfiguration {
   private final Locale[] locales;
   private final Properties[] prop;
   private final Pattern[][] pluralPatterns;
   private int defI = -1;
   private int i;

   public LangConfiguration(Locale[] locales, Locale select) {
      this.locales = locales;
      int count = locales.length;
      this.prop = new Properties[count];

      int keyLocale;
      for(keyLocale = 0; keyLocale < count; ++keyLocale) {
         Locale locale = locales[keyLocale];
         if (locale == null) {
            throw new NullPointerException("locale at " + keyLocale + " is null");
         }

         InputStream in = this.getClass().getResourceAsStream("/lang/" + locale);
         if (in == null) {
            throw new RuntimeException("could not find file for: " + locale);
         }

         try {
            this.prop[keyLocale] = loadFromStream(in);
         } catch (IOException var12) {
            throw new RuntimeException("could not load file for: " + locale, var12);
         }

         if (locale.toString().equals("en_US")) {
            copyProperties(this.prop[keyLocale], this.properties, true);
         }
      }

      this.setSelected(select);
      this.pluralPatterns = new Pattern[count][0];
      keyLocale = -1;

      int i;
      for(i = 0; i < count; ++i) {
         if (locales[i].toString().equals("en_US")) {
            this.defI = i;
         }

         if (locales[i].toString().equals("ru_RU")) {
            keyLocale = i;
         }

         String pluralFormsValue = this.prop[i].getProperty("plural");
         if (pluralFormsValue != null) {
            String[] pluralForms = StringUtils.split(pluralFormsValue, ';');
            Pattern[] pluralFormsPatterns = new Pattern[pluralForms.length];

            for(int k = 0; k < pluralForms.length; ++k) {
               try {
                  pluralFormsPatterns[k] = Pattern.compile(pluralForms[k]);
               } catch (PatternSyntaxException var11) {
                  throw new RuntimeException("could not compile plural form \"" + pluralForms[k] + "\" (index: " + k + ") for " + locales[i]);
               }
            }

            this.pluralPatterns[i] = pluralFormsPatterns;
            log("Plural patterns for " + locales[i] + ":", pluralFormsPatterns);
         }
      }

      if (keyLocale != -1 && TLauncher.getDebug()) {
         Iterator var14 = this.prop[keyLocale].keySet().iterator();

         while(var14.hasNext()) {
            Object primaryKey = var14.next();

            for(int i = 0; i < count; ++i) {
               if (i != keyLocale && !this.prop[i].containsKey(primaryKey)) {
                  log("Missing key in " + locales[i] + ": " + primaryKey);
               }
            }
         }

         for(i = 0; i < count; ++i) {
            if (i != keyLocale) {
               Iterator var17 = this.prop[i].keySet().iterator();

               while(var17.hasNext()) {
                  Object redundantKey = var17.next();
                  if (!this.prop[keyLocale].containsKey(redundantKey)) {
                     log("Redundant key in " + locales[i] + ": " + redundantKey);
                  } else {
                     String key = redundantKey.toString();
                     if (key.endsWith(".plural")) {
                        String pluralValues = this.prop[i].getProperty(key);
                        if (pluralValues == null) {
                           throw new NullPointerException("plural key is null: " + key + " in locale " + locales[i]);
                        }

                        String[] plurals = StringUtils.split(pluralValues, ';');
                        if (plurals.length != this.pluralPatterns[i].length) {
                           throw new RuntimeException("incorrect plural forms count: " + key + " in locale " + locales[i]);
                        }
                     }
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
      label40: {
         value = this.nget(key);
         if (key == null) {
            if (value != null) {
               break label40;
            }
         } else if (!key.equals(value) && !StringUtils.isEmpty(value)) {
            break label40;
         }

         return null;
      }

      String[] variables = checkVariables(vars);

      for(int var = 0; var < variables.length; ++var) {
         String pluralReplacementValue = this.nget(key + '.' + var + ".plural");
         if (pluralReplacementValue == null) {
            value = StringUtils.replace(value, "%" + var, variables[var]);
         } else {
            String[] pluralReplacements = StringUtils.split(pluralReplacementValue, ';');

            for(int patternKey = 0; patternKey < this.pluralPatterns[this.i].length; ++patternKey) {
               if (this.pluralPatterns[this.i][patternKey].matcher(variables[var]).matches()) {
                  value = StringUtils.replace(value, "%" + var, StringUtils.replace(pluralReplacements[patternKey], "%n", variables[var]));
                  break;
               }
            }
         }
      }

      return value;
   }

   public String get(String key, Object... vars) {
      String value = this.nget(key, vars);
      return value == null ? key : value;
   }

   public String getDefault(String key) {
      return this.defI == -1 ? null : this.prop[this.defI].getProperty(key);
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

   private static void log(Object... o) {
      U.log("[Lang]", o);
   }
}
