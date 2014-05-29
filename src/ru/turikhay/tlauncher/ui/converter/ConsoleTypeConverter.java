package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

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

   public Class getObjectClass() {
      return Configuration.ConsoleType.class;
   }
}
