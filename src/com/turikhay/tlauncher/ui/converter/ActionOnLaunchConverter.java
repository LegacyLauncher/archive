package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ActionOnLaunchConverter extends LocalizableStringConverter {
   public ActionOnLaunchConverter() {
      super("settings.launch-action");
   }

   public Configuration.ActionOnLaunch fromString(String from) {
      return Configuration.ActionOnLaunch.get(from);
   }

   public String toValue(Configuration.ActionOnLaunch from) {
      return from.toString();
   }

   public String toPath(Configuration.ActionOnLaunch from) {
      return from.toString();
   }
}
