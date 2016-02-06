package ru.turikhay.tlauncher.ui.converter;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.configuration.Configuration;

public class LocaleConverter implements StringConverter {
   public String toString(Locale from) {
      String displayLang;
      try {
         displayLang = from.getDisplayLanguage(from);
         if (StringUtils.isEmpty(displayLang)) {
            throw new IllegalArgumentException();
         }
      } catch (Exception var4) {
         displayLang = from.getDisplayLanguage(Locale.US);
      }

      return from == null ? null : displayLang + " (" + from + ")";
   }

   public Locale fromString(String from) {
      return Configuration.getLocaleOf(from);
   }

   public String toValue(Locale from) {
      return from == null ? null : from.toString();
   }
}
