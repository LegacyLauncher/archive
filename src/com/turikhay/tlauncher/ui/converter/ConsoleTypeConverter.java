package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ConsoleTypeConverter extends LocalizableStringConverter {
   public ConsoleTypeConverter() {
      super("settings.console");
   }

   public Configuration.ConsoleType fromString(String from) {
      return Configuration.ConsoleType.get(from);
   }

   public String toValue(Configuration.ConsoleType from) {
      return from == null ? null : from.toString();
   }

   public String toPath(Configuration.ConsoleType from) {
      return from == null ? null : from.toString();
   }
}
