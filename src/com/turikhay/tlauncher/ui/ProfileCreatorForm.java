package com.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;

public class ProfileCreatorForm extends CenterPanel {
   private static final long serialVersionUID = -3246475706073813172L;
   UsernameField username = new UsernameField(this);
   JPasswordField password = new JPasswordField();
   LocalizableCheckbox premiumBox = new LocalizableCheckbox("profile.premium");
   JProgressBar progressBar;

   public ProfileCreatorForm(TLauncherFrame f) {
      super(f);
      this.premiumBox.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            ProfileCreatorForm.this.password.setEnabled(newstate);
            ProfileCreatorForm.this.username.setPlaceholder("profile." + (newstate ? "e-mail" : "username"));
         }
      });
      this.progressBar = new JProgressBar();
      this.progressBar.setVisible(true);
      this.progressBar.setPreferredSize(new Dimension(200, 20));
      this.progressBar.setIndeterminate(true);
      this.add(this.del(0));
      this.add(sepPan(new Component[]{this.username}));
      this.add(sepPan(new Component[]{this.premiumBox}));
      this.add(sepPan(new Component[]{this.password}));
      this.add(this.del(0));
      this.add(sepPan(new Component[]{new LocalizableButton("profile.creator.next")}));
      this.add(sepPan(new Component[]{this.progressBar}));
   }
}
