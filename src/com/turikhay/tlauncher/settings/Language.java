package com.turikhay.tlauncher.settings;

import java.io.IOException;
import java.net.URL;

public class Language extends Settings {
   static URL langfile = Language.class.getResource("/lang.ini");

   public Language() throws IOException {
      super(langfile);
   }
}
