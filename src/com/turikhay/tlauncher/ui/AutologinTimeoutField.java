package com.turikhay.tlauncher.ui;

public class AutologinTimeoutField extends ExtendedTextField {
   private static final long serialVersionUID = 104141941185197117L;
   private static final int MAX_TIMEOUT = 10;

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

      return cur >= 1 && cur <= 10 ? true : this.setError(this.l.get("settings.tlauncher.autologin.incorrect", "s", 10));
   }

   public int getSpecialValue() {
      String val = this.getValue();
      return val != null && !val.equals("") ? Integer.parseInt(val) : 3;
   }
}
