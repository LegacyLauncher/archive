package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;

public class PlayButton extends LocalizableButton {
   private static final long serialVersionUID = 6944074583143406549L;
   private PlayButton.PlayButtonState state;
   private final LoginForm loginForm;

   PlayButton(LoginForm lf) {
      this.loginForm = lf;
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            PlayButton.this.loginForm.callLogin();
         }
      });
      this.setFont(this.getFont().deriveFont(1).deriveFont(16.0F));
      this.setState(PlayButton.PlayButtonState.PLAY);
   }

   public PlayButton.PlayButtonState getState() {
      return this.state;
   }

   public void setState(PlayButton.PlayButtonState state) {
      if (state == null) {
         throw new NullPointerException();
      } else {
         this.state = state;
         this.setText(state.getPath());
      }
   }

   public void updateState() {
      VersionSyncInfo vs = this.loginForm.versions.getVersion();
      if (vs != null) {
         boolean installed = vs.isInstalled();
         boolean force = this.loginForm.checkbox.forceupdate.getState();
         if (!installed) {
            this.setState(PlayButton.PlayButtonState.INSTALL);
         } else {
            this.setState(force ? PlayButton.PlayButtonState.REINSTALL : PlayButton.PlayButtonState.PLAY);
         }

      }
   }

   public static enum PlayButtonState {
      REINSTALL("loginform.enter.reinstall"),
      INSTALL("loginform.enter.install"),
      PLAY("loginform.enter");

      private final String path;

      private PlayButtonState(String path) {
         this.path = path;
      }

      public String getPath() {
         return this.path;
      }
   }
}
