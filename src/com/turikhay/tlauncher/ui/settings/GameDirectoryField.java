package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.text.CheckableTextField;
import com.turikhay.util.MinecraftUtil;
import java.io.File;

public class GameDirectoryField extends CheckableTextField implements SettingsField {
   private static final long serialVersionUID = 9048714882203326864L;
   private boolean saveable = true;

   GameDirectoryField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   protected String check(String text) {
      if (text == null) {
         return "settings.client.gamedir.invalid";
      } else {
         File f = new File(text);
         return !f.canRead() ? "settings.client.gamedir.noaccess" : null;
      }
   }

   public String getSettingsPath() {
      return "minecraft.gamedir";
   }

   public boolean isValueValid() {
      return this.check();
   }

   public void setToDefault() {
      this.setValue(MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public void setSaveable(boolean val) {
      this.saveable = val;
   }
}
