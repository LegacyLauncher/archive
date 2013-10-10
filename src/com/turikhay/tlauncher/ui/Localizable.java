package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;

public class Localizable {
   public static void setLang(Settings l) {
      LocalizableLabel.l = l;
      LocalizableCheckbox.l = l;
      LocalizableTextField.l = l;
      LocalizableButton.l = l;
   }
}
