package ru.turikhay.tlauncher.ui.converter;

import java.util.Locale;
import ru.turikhay.tlauncher.configuration.Configuration;

public class LocaleConverter implements StringConverter {
   public String toString(Locale from) {
      return from == null ? null : from.getDisplayCountry(Locale.US) + " (" + from.toString() + ")";
   }

   public Locale fromString(String from) {
      return Configuration.getLocaleOf(from);
   }

   public String toValue(Locale from) {
      return from == null ? null : from.toString();
   }

   public Class getObjectClass() {
      return Locale.class;
   }
}
