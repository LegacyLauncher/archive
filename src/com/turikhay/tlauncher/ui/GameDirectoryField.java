package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.MinecraftUtil;
import java.io.File;

public class GameDirectoryField extends ExtendedTextField {
   private static final long serialVersionUID = 9048714882203326864L;

   GameDirectoryField(SettingsForm settingsform) {
      super((CenterPanel)settingsform);
   }

   public void setText(String dir) {
      if (dir == null || !(new File(dir)).isDirectory()) {
         dir = MinecraftUtil.getWorkingDirectory().toString();
      }

      super.setText(dir);
   }

   protected boolean check(String text) {
      File f = new File(text);
      return !f.exists() || f.canRead() && f.canWrite();
   }
}
