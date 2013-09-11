package com.turikhay.tlauncher.ui;

import net.minecraft.launcher_.OperatingSystem;

public class MemoryField extends ExtendedTextField {
   private static final long serialVersionUID = 104141941185197117L;
   private long maxMB = Runtime.getRuntime().maxMemory() / 1024L / 1024L;

   MemoryField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   protected boolean check(String text) {
      if (text != null && !text.equals("")) {
         boolean var2 = true;

         int cur;
         try {
            cur = Integer.parseInt(text);
         } catch (Exception var4) {
            return this.setError(this.l.get("settings.java.memory.parse"));
         }

         return cur >= 0 && (long)cur <= this.maxMB ? true : this.setError(this.l.get("settings.java.memory.incorrect", "s", this.maxMB));
      } else {
         return true;
      }
   }

   public int getSpecialValue() {
      String val = this.getValue();
      return val != null && !val.equals("") ? Integer.parseInt(val) : OperatingSystem.getRecommendedMemory();
   }
}
