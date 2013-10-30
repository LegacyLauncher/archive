package com.turikhay.tlauncher.ui;

import java.io.File;
import net.minecraft.launcher_.OperatingSystem;

public class JavaExecutableField extends ExtendedTextField implements SettingsField {
   private static final long serialVersionUID = 2221135591155035960L;

   JavaExecutableField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   public void setText(String dir) {
      if (dir == null || !(new File(dir)).isFile()) {
         dir = OperatingSystem.getCurrentPlatform().getJavaDir();
      }

      super.setText(dir);
   }

   protected boolean check(String text) {
      return true;
   }

   public String getSettingsPath() {
      return "minecraft.javadir";
   }

   public boolean isValueValid() {
      return this.getValue() != null;
   }

   public void setToDefault() {
      this.setValue((String)null);
   }
}
