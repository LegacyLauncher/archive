package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.util.U;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;

public class ProfileCreatorForm extends CenterPanel implements AuthenticatorListener {
   private static final long serialVersionUID = -3246475706073813172L;
   UsernameField username;
   JPasswordField password;
   LocalizableCheckbox premiumBox;
   LocalizableButton next;
   JProgressBar progressBar;

   public ProfileCreatorForm(TLauncherFrame f) {
      super(f);
      this.username = new UsernameField(this, UsernameField.UsernameState.USERNAME);
      this.password = new JPasswordField();
      this.premiumBox = new LocalizableCheckbox("profile.premium");
      this.premiumBox.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            ProfileCreatorForm.this.password.setEnabled(newstate);
            ProfileCreatorForm.this.username.setPlaceholder("profile." + (newstate ? "e-mail" : "username"));
            ProfileCreatorForm.this.username.setState(newstate ? UsernameField.UsernameState.EMAIL : UsernameField.UsernameState.USERNAME);
            ProfileCreatorForm.this.username.check(true);
            ProfileCreatorForm.this.defocus();
         }
      });
      this.next = new LocalizableButton("profile.creator.next");
      this.next.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ProfileCreatorForm.this.doAuth();
            ProfileCreatorForm.this.defocus();
         }
      });
      this.progressBar = new JProgressBar();
      this.progressBar.setPreferredSize(new Dimension(200, 20));
      this.add(this.error);
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.username}));
      this.add(sepPan(new Component[]{this.premiumBox}));
      this.add(sepPan(new Component[]{this.password}));
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.next}));
      this.add(sepPan(new Component[]{this.progressBar}));
   }

   private void doAuth() {
      if (!this.isBlocked()) {
         if (this.username.check(false)) {
            if (this.password.getPassword().length != 0) {
               Authenticator auth = new Authenticator();
               auth.setClientToken(TLauncher.getClientToken());
               auth.setUsername(this.username.getValue());
               auth.setPassword(this.password.getPassword());
               auth.asyncPass(this);
            }
         }
      }
   }

   public void onAuthPassing(Authenticator auth) {
      this.block("AUTH");
      this.progressBar.setIndeterminate(true);
   }

   public void onAuthPassingError(Authenticator auth, Throwable e) {
      this.unblock("AUTH");
      this.progressBar.setIndeterminate(false);
   }

   public void onAuthPassed(Authenticator auth) {
      this.unblock("AUTH");
      this.progressBar.setIndeterminate(false);
      U.log(auth);
   }
}
