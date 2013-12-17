package com.turikhay.tlauncher.ui;

import com.turikhay.util.MinecraftUtil;
import java.io.File;

public class GameDirectoryField extends ExtendedTextField implements SettingsField {
   private static final long serialVersionUID = 9048714882203326864L;
   private boolean saveable = true;

   GameDirectoryField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   public void setValue(String dir) {
      if (dir == null || !(new File(dir)).isDirectory()) {
         dir = MinecraftUtil.getDefaultWorkingDirectory().toString();
      }

      super.setText(dir);
   }

   protected boolean check(String text) {
      File f = new File(text);
      return !f.exists() || f.canRead() && f.canWrite();
   }

   public String getSettingsPath() {
      return "minecraft.gamedir";
   }

   public boolean isValueValid() {
      return this.getValue() != null;
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
