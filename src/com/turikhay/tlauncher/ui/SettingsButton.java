package com.turikhay.tlauncher.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsButton extends ImageButton {
   private static final long serialVersionUID = 1321382157134544911L;
   private final LoginForm lf;

   SettingsButton(LoginForm loginform) {
      this.lf = loginform;
      this.image = loadImage("settings.png");
      this.rotation = ImageButton.ImageRotation.CENTER;
      this.setPreferredSize(new Dimension(30, this.getHeight()));
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SettingsButton.this.lf.settings.save()) {
               SettingsButton.this.lf.f.mp.toggleSettings();
            }

            SettingsButton.this.lf.defocus();
         }
      });
      this.initImage();
   }
}
