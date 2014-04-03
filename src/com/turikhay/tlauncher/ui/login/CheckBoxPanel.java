package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import com.turikhay.tlauncher.ui.swing.CheckBoxListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import net.minecraft.launcher.updater.VersionSyncInfo;

public class CheckBoxPanel extends BlockablePanel implements LoginListener {
   private static final long serialVersionUID = 768489049585749260L;
   public final LocalizableCheckbox autologin;
   public final LocalizableCheckbox forceupdate;
   private boolean state;
   private final LoginForm loginForm;

   CheckBoxPanel(LoginForm lf) {
      BoxLayout lm = new BoxLayout(this, 3);
      this.setLayout(lm);
      this.setOpaque(false);
      this.setAlignmentX(0.5F);
      this.loginForm = lf;
      this.autologin = new LocalizableCheckbox("loginform.checkbox.autologin", lf.global.getBoolean("login.auto"));
      this.autologin.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            CheckBoxPanel.this.loginForm.autologin.setEnabled(newstate);
            if (newstate) {
               Alert.showLocAsyncMessage("loginform.checkbox.autologin.tip.title", "loginform.checkbox.autologin.tip", Localizable.get("loginform.checkbox.autologin.tip.arg"));
            }

         }
      });
      this.forceupdate = new LocalizableCheckbox("loginform.checkbox.forceupdate");
      this.forceupdate.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            CheckBoxPanel.this.state = newstate;
            CheckBoxPanel.this.loginForm.buttons.play.updateState();
         }
      });
      this.add(this.autologin);
      this.add(Box.createHorizontalGlue());
      this.add(this.forceupdate);
   }

   public void onLogin() throws LoginException {
      VersionSyncInfo syncInfo = this.loginForm.versions.getVersion();
      if (syncInfo != null) {
         boolean supporting = syncInfo.hasRemote();
         boolean installed = syncInfo.isInstalled();
         if (this.state) {
            if (!supporting) {
               Alert.showLocError("forceupdate.local");
               throw new LoginException("Cannot update local version!");
            }

            if (installed && !Alert.showLocQuestion("forceupdate.question")) {
               throw new LoginException("User has cancelled force updating.");
            }
         }

      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
