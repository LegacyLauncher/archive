package com.turikhay.tlauncher.ui.settings;

public abstract class SettingsFieldChangeListener extends SettingsFieldListener {
   protected void onChange(SettingsHandler handler, String oldValue, String newValue) {
      if (newValue != null || oldValue != null) {
         if (newValue == null || !newValue.equals(oldValue)) {
            this.onChange(oldValue, newValue);
         }
      }
   }

   protected abstract void onChange(String var1, String var2);
}
