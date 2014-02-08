package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.text.CheckableTextField;
import java.io.File;
import net.minecraft.launcher.OperatingSystem;

public class JavaExecutableField extends CheckableTextField implements SettingsField {
   private static final long serialVersionUID = 2221135591155035960L;
   private boolean saveable = true;

   JavaExecutableField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   public void setText(String dir) {
      if (dir == null || !(new File(dir)).exists()) {
         dir = OperatingSystem.getCurrentPlatform().getJavaDir();
      }

      super.setText(dir);
   }

   protected String check(String text) {
      return text == null ? "settings.java.path.invalid" : null;
   }

   public String getSettingsPath() {
      return "minecraft.javadir";
   }

   public boolean isValueValid() {
      return this.check();
   }

   public void setToDefault() {
      this.setValue(OperatingSystem.getCurrentPlatform().getJavaDir());
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public void setSaveable(boolean val) {
      this.saveable = val;
   }
}
