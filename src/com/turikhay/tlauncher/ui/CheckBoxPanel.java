package com.turikhay.tlauncher.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import net.minecraft.launcher.updater.VersionSyncInfo;

public class CheckBoxPanel extends BlockablePanel implements LoginListener {
   private static final long serialVersionUID = 1808335203922301270L;
   private final LoginForm lf;
   LocalizableCheckbox autologinbox;
   LocalizableCheckbox forceupdatebox;
   private boolean forceupdate;

   CheckBoxPanel(LoginForm loginform, boolean autologin_enabled, boolean console_enabled) {
      this.lf = loginform;
      BoxLayout lm = new BoxLayout(this, 3);
      this.setLayout(lm);
      this.setOpaque(false);
      this.setAlignmentX(0.5F);
      this.setAlignmentY(0.5F);
      this.autologinbox = new LocalizableCheckbox("loginform.checkbox.autologin", autologin_enabled);
      this.autologinbox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            CheckBoxPanel.this.lf.setAutoLogin(newstate);
            CheckBoxPanel.this.lf.defocus();
         }
      });
      this.forceupdatebox = new LocalizableCheckbox("loginform.checkbox.forceupdate");
      this.forceupdatebox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            CheckBoxPanel.this.forceupdate = newstate;
            CheckBoxPanel.this.onForceUpdateChanged();
            CheckBoxPanel.this.lf.defocus();
         }
      });
      this.add(this.autologinbox);
      this.add(Box.createHorizontalGlue());
      this.add(this.forceupdatebox);
   }

   public void setForceUpdate(boolean s) {
      this.forceupdate = s;
      this.onForceUpdateChanged();
   }

   public boolean getForceUpdate() {
      return this.forceupdate;
   }

   private void onForceUpdateChanged() {
      this.lf.buttons.updateEnterButton();
      this.forceupdatebox.setState(this.forceupdate);
   }

   void uncheckAutologin() {
      this.autologinbox.setState(false);
   }

   protected void blockElement(Object reason) {
      this.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.setEnabled(true);
   }

   public boolean onLogin() {
      VersionSyncInfo syncInfo = this.lf.versionchoice.getSyncVersionInfo();
      boolean supporting = syncInfo.isOnRemote();
      boolean installed = syncInfo.isInstalled();
      if (this.getForceUpdate()) {
         if (!supporting) {
            Alert.showWarning("forceupdate.onlylibraries");
         } else if (installed && !Alert.showQuestion("forceupdate.question", true)) {
            return false;
         }
      }

      return true;
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
      this.setForceUpdate(false);
   }
}
