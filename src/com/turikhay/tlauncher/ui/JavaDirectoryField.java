package com.turikhay.tlauncher.ui;

import java.io.File;
import net.minecraft.launcher_.OperatingSystem;

public class JavaDirectoryField extends ExtendedTextField {
   private static final long serialVersionUID = 2221135591155035960L;

   JavaDirectoryField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   public void setText(String dir) {
      if (dir == null || !(new File(dir)).isDirectory()) {
         dir = OperatingSystem.getCurrentPlatform().getJavaDir();
      }

      super.setText(dir);
   }

   protected boolean check(String text) {
      File f = new File(text);
      return !f.exists() || f.canRead() && f.canWrite();
   }
}
