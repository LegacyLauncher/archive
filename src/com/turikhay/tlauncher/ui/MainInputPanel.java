package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class MainInputPanel extends BlockablePanel implements LocalizableComponent, LoginListener {
   private static final long serialVersionUID = 296073104610204659L;
   private final LoginForm lf;
   private boolean saveable;
   UsernameField field;

   MainInputPanel(LoginForm loginform, String username) {
      this.lf = loginform;
      LayoutManager lm = new GridLayout(0, 1);
      this.setLayout(lm);
      this.setOpaque(false);
      this.field = new UsernameField(this.lf);
      this.field.setValue(username);
      this.saveable = this.lf.s.isSaveable("login.username");
      this.field.setEnabled(this.saveable);
      this.add(this.field);
   }

   protected void blockElement(Object reason) {
      if (this.saveable) {
         this.field.setEnabled(false);
      }

   }

   protected void unblockElement(Object reason) {
      if (this.saveable) {
         this.field.setEnabled(true);
      }

   }

   public void updateLocale() {
      this.field.setPlaceholder("loginform.username");
   }

   public boolean onLogin() {
      return this.field.check(false);
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
