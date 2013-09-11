package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.MinecraftUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import net.minecraft.launcher_.OperatingSystem;

public class FolderButton extends ImageButton {
   private static final long serialVersionUID = 1621745146166800209L;
   private final FolderButton instance = this;
   private final LoginForm lf;
   private final Settings l;
   private final File dir;

   FolderButton(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.l;
      this.image = loadImage("folder.png");
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.dir = MinecraftUtil.getWorkingDirectory();
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            FolderButton.this.instance.openFolder();
            FolderButton.this.lf.defocus();
         }
      });
   }

   public void openFolder() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            if (!OperatingSystem.openFile(FolderButton.this.dir)) {
               Alert.showError(FolderButton.this.l.get("support.error.title"), FolderButton.this.l.get("support.error"), (Object)FolderButton.this.dir);
            }

         }
      });
   }
}
