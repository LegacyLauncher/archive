package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class SettingsCheckBox extends LocalizableCheckbox implements SettingsField {
   private static final long serialVersionUID = -2540132118355226609L;

   SettingsCheckBox(String path) {
      super(path);
   }

   public String getSettingsValue() {
      return this.isSelected() ? "true" : "false";
   }

   public void setSettingsValue(String value) {
      this.setSelected(Boolean.parseBoolean(value));
   }

   public boolean isValueValid() {
      return true;
   }
}
