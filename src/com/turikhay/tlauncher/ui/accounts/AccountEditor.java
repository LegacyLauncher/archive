package com.turikhay.tlauncher.ui.accounts;

import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.CheckBoxListener;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableButton;
import com.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import com.turikhay.tlauncher.ui.login.UsernameField;
import com.turikhay.tlauncher.ui.progress.ProgressBar;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.text.ExtendedPasswordField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AccountEditor extends CenterPanel {
   private static final long serialVersionUID = 7061277150214976212L;
   private final AccountEditorScene scene;
   public final UsernameField username;
   public final ExtendedPasswordField password;
   public final LocalizableCheckbox premiumBox;
   public final LocalizableButton save;
   public final ProgressBar progressBar;

   public AccountEditor(AccountEditorScene sc) {
      super(squareInsets);
      this.scene = sc;
      this.username = new UsernameField(this, UsernameField.UsernameState.USERNAME);
      this.password = new ExtendedPasswordField();
      this.password.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountEditor.this.defocus();
            AccountEditor.this.scene.handler.saveEditor();
         }
      });
      this.premiumBox = new LocalizableCheckbox("account.premium");
      this.premiumBox.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            if (newstate && !AccountEditor.this.password.hasPassword()) {
               AccountEditor.this.password.setText((String)null);
            }

            AccountEditor.this.password.setEnabled(newstate);
            AccountEditor.this.username.setState(newstate ? UsernameField.UsernameState.EMAIL : UsernameField.UsernameState.USERNAME);
            AccountEditor.this.defocus();
         }
      });
      this.save = new LocalizableButton("account.save", new Object[0]);
      this.save.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            AccountEditor.this.defocus();
            AccountEditor.this.scene.handler.saveEditor();
         }
      });
      this.progressBar = new ProgressBar();
      this.progressBar.setPreferredSize(new Dimension(200, 20));
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.username}));
      this.add(sepPan(new Component[]{this.premiumBox}));
      this.add(sepPan(new Component[]{this.password}));
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.save}));
      this.add(sepPan(new Component[]{this.progressBar}));
   }

   public void fill(Account account) {
      this.premiumBox.setSelected(account.hasLicense());
      this.username.setText(account.getUsername());
      this.password.setText((String)null);
   }

   public void clear() {
      this.premiumBox.setSelected(false);
      this.username.setText((String)null);
      this.password.setText((String)null);
   }

   public Account get() {
      Account account = new Account();
      account.setUsername(this.username.getValue());
      if (this.premiumBox.isSelected()) {
         account.setHasLicense(true);
         if (this.password.hasPassword()) {
            account.setPassword(this.password.getPassword());
         }
      }

      return account;
   }

   public Insets getInsets() {
      return squareInsets;
   }

   public void block(Object reason) {
      super.block(reason);
      this.password.setEnabled(this.premiumBox.isSelected());
      if (!reason.equals("empty")) {
         this.progressBar.setIndeterminate(true);
      }

   }

   public void unblock(Object reason) {
      super.unblock(reason);
      this.password.setEnabled(this.premiumBox.isSelected());
      if (!reason.equals("empty")) {
         this.progressBar.setIndeterminate(false);
      }

   }
}
