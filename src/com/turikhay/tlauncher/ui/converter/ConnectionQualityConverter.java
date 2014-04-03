package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ConnectionQualityConverter extends LocalizableStringConverter {
   public ConnectionQualityConverter() {
      super("settings.connection");
   }

   public Configuration.ConnectionQuality fromString(String from) {
      return Configuration.ConnectionQuality.get(from);
   }

   public String toValue(Configuration.ConnectionQuality from) {
      return from.toString();
   }

   public String toPath(Configuration.ConnectionQuality from) {
      return from.toString();
   }
}
