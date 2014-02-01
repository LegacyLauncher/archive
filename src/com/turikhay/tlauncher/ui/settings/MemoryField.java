package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.text.CheckableTextField;
import net.minecraft.launcher.OperatingSystem;

public class MemoryField extends CheckableTextField {
   private static final long serialVersionUID = 104141941185197117L;
   private long maxMB = Runtime.getRuntime().maxMemory() / 1024L / 1024L;

   MemoryField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   protected String check(String text) {
      if (text != null && !text.equals("")) {
         boolean var2 = true;

         int cur;
         try {
            cur = Integer.parseInt(text);
         } catch (Exception var4) {
            return "settings.java.memory.parse";
         }

         return cur >= 0 && (long)cur <= this.maxMB ? null : "settings.java.memory.incorrect";
      } else {
         return null;
      }
   }

   public int getSpecialValue() {
      String val = this.getValue();
      return val != null && !val.equals("") ? Integer.parseInt(val) : OperatingSystem.getCurrentPlatform().getRecommendedMemory();
   }
}
