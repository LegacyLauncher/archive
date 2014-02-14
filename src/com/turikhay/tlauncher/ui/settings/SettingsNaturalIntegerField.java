package com.turikhay.tlauncher.ui.settings;

public class SettingsNaturalIntegerField extends SettingsTextField {
   private static final long serialVersionUID = -7930510655707946312L;

   SettingsNaturalIntegerField() {
   }

   SettingsNaturalIntegerField(String prompt) {
      super(prompt);
   }

   public boolean isValueValid() {
      try {
         Integer.parseInt(this.getSettingsValue());
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
