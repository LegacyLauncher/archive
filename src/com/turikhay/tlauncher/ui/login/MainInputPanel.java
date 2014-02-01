package com.turikhay.tlauncher.ui.login;

import com.turikhay.tlauncher.ui.block.BlockablePanel;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainInputPanel extends BlockablePanel implements LoginListener {
   private static final long serialVersionUID = 296073104610204659L;
   private final LoginForm lf;
   public final UsernameField field;

   MainInputPanel(LoginForm loginform, String username) {
      this.lf = loginform;
      LayoutManager lm = new GridLayout(0, 1);
      this.setLayout(lm);
      this.setOpaque(false);
      this.field = new UsernameField(this.lf, UsernameField.UsernameState.USERNAME);
      this.field.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            MainInputPanel.this.lf.callLogin();
         }
      });
      this.field.setValue(username);
      this.add(this.field);
   }

   public void block(Object reason) {
      this.field.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.field.setEnabled(true);
   }

   public boolean onLogin() {
      return true;
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
