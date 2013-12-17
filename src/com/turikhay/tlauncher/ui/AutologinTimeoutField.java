package com.turikhay.tlauncher.ui;

public class AutologinTimeoutField extends ExtendedTextField implements LocalizableComponent, SettingsField {
   private static final long serialVersionUID = 104141941185197117L;
   private static final int DEFAULT_TIMEOUT = 3;
   private static final int MAX_TIMEOUT = 10;
   private boolean saveable = true;

   AutologinTimeoutField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   protected boolean check(String text) {
      boolean var2 = true;

      int cur;
      try {
         cur = Integer.parseInt(text);
      } catch (Exception var4) {
         return this.setError(this.l.get("settings.tlauncher.autologin.parse"));
      }

      return cur >= 2 && cur <= 10 ? true : this.setError(this.l.get("settings.tlauncher.autologin.incorrect", "s", 10));
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
