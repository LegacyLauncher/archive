package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.ImageButton;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.async.AsyncThread;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import net.minecraft.launcher.OperatingSystem;

public class FolderButton extends ImageButton {
   private static final long serialVersionUID = 1621745146166800209L;
   private final FolderButton instance = this;
   private final LoginForm lf;
   private final LangConfiguration l;

   FolderButton(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.lang;
      this.image = loadImage("folder.png");
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            FolderButton.this.instance.openFolder();
            FolderButton.this.lf.defocus();
         }
      });
      this.initImage();
   }

   public void openFolder() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            File dir = MinecraftUtil.getWorkingDirectory();
            if (!OperatingSystem.openFile(dir)) {
               Alert.showError(FolderButton.this.l.get("folder.error.title"), FolderButton.this.l.get("folder.error"), (Object)dir);
            }

         }
      });
   }
}
