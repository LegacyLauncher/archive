package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.MinecraftUtil;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.minecraft.launcher_.OperatingSystem;

public class SettingsButton extends ImageButton {
   private static final long serialVersionUID = 1321382157134544911L;
   private final LoginForm lf;
   private final Settings l;

   SettingsButton(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.l;
      this.image = loadImage("folder.png");
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.setPreferredSize(new Dimension(30, this.getHeight()));
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsButton.this.openSettings();
            SettingsButton.this.lf.defocus();
         }
      });
   }

   public void openSettings() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            if (!OperatingSystem.openFile(MinecraftUtil.getWorkingDirectory())) {
               Alert.showError(SettingsButton.this.l.get("settings.error.folder.title"), SettingsButton.this.l.get("settings.error.folder"), (Object)MinecraftUtil.getWorkingDirectory());
            }

         }
      });
   }
}
