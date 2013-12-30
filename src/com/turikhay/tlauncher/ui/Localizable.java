package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;

public class Localizable {
   private static Settings lang;

   public static void setLang(Settings l) {
      lang = l;
      LocalizableLabel.l = l;
      LocalizableCheckbox.l = l;
      LocalizableTextField.l = l;
      LocalizableButton.l = l;
      LocalizableTransparentButton.l = l;
      TextPopup.l = l;
   }

   public static Settings get() {
      return lang;
   }
}
