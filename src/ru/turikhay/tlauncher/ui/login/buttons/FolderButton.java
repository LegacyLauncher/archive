package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.async.AsyncThread;

public class FolderButton extends ImageButton implements Unblockable {
   final LoginForm lf;

   FolderButton(LoginForm loginform) {
      this.lf = loginform;
      this.image = loadImage("folder.png");
      final Runnable run = new Runnable() {
         public void run() {
            OS.openFolder(MinecraftUtil.getWorkingDirectory());
         }
      };
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AsyncThread.execute(run);
         }
      });
   }
}
