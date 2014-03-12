package com.turikhay.tlauncher.ui.login.buttons;

import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.swing.ImageButton;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.async.AsyncThread;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import net.minecraft.launcher.OperatingSystem;

public class FolderButton extends ImageButton implements Blockable {
   private static final long serialVersionUID = 1621745146166800209L;
   private final FolderButton instance = this;
   private final LoginForm lf;

   FolderButton(LoginForm loginform) {
      this.lf = loginform;
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

   void openFolder() {
      AsyncThread.execute(new Runnable() {
         public void run() {
            File dir = MinecraftUtil.getWorkingDirectory();
            OperatingSystem.openFile(dir);
         }
      });
   }

   public void block(Object reason) {
   }

   public void unblock(Object reason) {
   }
}