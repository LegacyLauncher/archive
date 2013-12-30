package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class ProfileChoicePanel extends BlockablePanel implements LoginListener {
   private static final long serialVersionUID = -4181724767910138064L;
   ProfileChoice pc;

   ProfileChoicePanel(LoginForm lf) {
      LayoutManager lm = new GridLayout(0, 1);
      this.setLayout(lm);
      this.setOpaque(false);
      this.pc = new ProfileChoice(lf.t.getProfileLoader());
      this.add(this.pc);
   }

   protected void blockElement(Object reason) {
   }

   protected void unblockElement(Object reason) {
   }

   public boolean onLogin() {
      return true;
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
