package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.GridLayout;
import java.awt.LayoutManager;

public class MainInputPanel extends BlockablePanel {
   private static final long serialVersionUID = 296073104610204659L;
   private final LoginForm lf;
   private final Settings l;
   UsernameField field;

   MainInputPanel(LoginForm loginform, String username) {
      this.lf = loginform;
      this.l = this.lf.l;
      LayoutManager lm = new GridLayout(1, 1);
      this.setLayout(lm);
      this.field = new UsernameField(this.lf, username, this.l.get("loginform.username"), 20);
      this.add(this.field);
   }

   protected void blockElement(Object reason) {
      this.field.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.field.setEnabled(true);
   }
}
