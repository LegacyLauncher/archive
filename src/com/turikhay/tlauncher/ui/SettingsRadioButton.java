package com.turikhay.tlauncher.ui;

public class SettingsRadioButton extends LocalizableRadioButton {
   private static final long serialVersionUID = 1L;
   private final String value;

   public SettingsRadioButton(String path, String value) {
      super(path);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }
}
