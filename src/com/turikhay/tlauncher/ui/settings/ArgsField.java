package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class ArgsField extends LocalizableTextField implements SettingsField {
   private static final long serialVersionUID = -5279771273100196802L;
   private String settingspath;
   private boolean saveable = true;

   ArgsField(SettingsForm sf, String placeholder, String settingspath) {
      super(sf, placeholder, (String)null);
      this.settingspath = settingspath;
   }

   protected boolean check(String text) {
      return true;
   }

   public boolean isValueValid() {
      return true;
   }

   public String getSettingsPath() {
      return this.settingspath;
   }

   public void setToDefault() {
      this.setValue((String)null);
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public void setSaveable(boolean val) {
      this.saveable = val;
   }
}
