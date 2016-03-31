package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.ui.block.Unblockable;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

public class FolderButton extends LocalizableButton implements Unblockable {
   final LoginForm lf;

   FolderButton(LoginForm loginform) {
      this.lf = loginform;
      this.setToolTipText("loginform.button.folder");
      this.setIcon(Images.getIcon("folder.png", SwingUtil.magnify(16), SwingUtil.magnify(16)));
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

   public Insets getInsets() {
      return SwingUtil.magnify(super.getInsets());
   }
}
