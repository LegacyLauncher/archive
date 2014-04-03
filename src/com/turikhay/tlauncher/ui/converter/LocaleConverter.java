package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;
import java.util.Locale;

public class LocaleConverter extends LocalizableStringConverter {
   public LocaleConverter() {
      super((String)null);
   }

   public String toString(Locale from) {
      return from.getDisplayCountry(Locale.US) + " (" + from.toString() + ")";
   }

   public Locale fromString(String from) {
      return Configuration.getLocaleOf(from);
   }

   public String toValue(Locale from) {
      return from == null ? null : from.toString();
   }

   public String toPath(Locale from) {
      return null;
   }
}
