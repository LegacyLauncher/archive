package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.Checkbox;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;

public class CheckBoxPanel extends BlockablePanel implements LoginListener {
   private static final long serialVersionUID = 1808335203922301270L;
   private final LoginForm lf;
   private final Settings l;
   Checkbox autologinbox;
   Checkbox forceupdatebox;
   private boolean forceupdate;

   CheckBoxPanel(LoginForm loginform, boolean autologin_enabled, boolean console_enabled) {
      this.lf = loginform;
      this.l = this.lf.l;
      LayoutManager lm = new BoxLayout(this, 1);
      this.setLayout(lm);
      this.autologinbox = new Checkbox(this.l.get("loginform.checkbox.autologin"));
      this.autologinbox.setState(autologin_enabled);
      this.autologinbox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            CheckBoxPanel.this.lf.setAutoLogin(newstate);
         }
      });
      this.forceupdatebox = new Checkbox(this.l.get("loginform.checkbox.forceupdate"));
      this.forceupdatebox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            CheckBoxPanel.this.forceupdate = newstate;
            CheckBoxPanel.this.onForceUpdateChanged();
         }
      });
      this.add(this.autologinbox);
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

   public void onLogin() {
      this.forceupdate = false;
      this.onForceUpdateChanged();
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
