package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.text.CheckableTextField;

public class AutologinTimeoutField extends CheckableTextField implements LocalizableComponent, SettingsField {
   private static final long serialVersionUID = 104141941185197117L;
   private static final int DEFAULT_TIMEOUT = 3;
   private static final int MAX_TIMEOUT = 10;
   private boolean saveable = true;

   AutologinTimeoutField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   protected String check(String text) {
      boolean var2 = true;

      int cur;
      try {
         cur = Integer.parseInt(text);
      } catch (Exception var4) {
         return "settings.tlauncher.autologin.parse";
      }

      return cur >= 2 && cur <= 10 ? null : "settings.tlauncher.autologin.incorrect";
   }

   public void updateLocale() {
      this.check();
   }

   public String getSettingsPath() {
      return "login.auto.timeout";
   }

   public boolean isValueValid() {
      String val = this.getValue();
      return val != null && !val.equals("");
   }

   public void setToDefault() {
      this.setValue(3);
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public void setSaveable(boolean val) {
      this.saveable = val;
   }
}
