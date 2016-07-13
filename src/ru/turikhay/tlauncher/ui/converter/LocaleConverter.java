package ru.turikhay.tlauncher.ui.converter;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.configuration.Configuration;

public class LocaleConverter implements StringConverter {
   public String toString(Locale from, Locale format) {
      String displayLang;
      try {
         displayLang = from.getDisplayLanguage(format);
         if (StringUtils.isEmpty(displayLang)) {
            throw new IllegalArgumentException();
         }
      } catch (Exception var5) {
         displayLang = from.getDisplayLanguage(Locale.US);
      }

      return from == null ? null : displayLang + " (" + from + ")";
   }

   public String toString(Locale from) {
      return this.toString(from, from);
   }

   public Locale fromString(String from) {
      return Configuration.getLocaleOf(from);
   }

   public String toValue(Locale from) {
      return from == null ? null : from.toString();
   }
}
