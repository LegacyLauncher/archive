package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonPanel extends BlockablePanel {
   private static final long serialVersionUID = 5873050319650201358L;
   private final LoginForm lf;
   private final Settings l;
   Button enter;
   Button cancel;
   SettingsButton settings;
   SupportButton support;

   public ButtonPanel(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.l;
      BorderLayout lm = new BorderLayout();
      lm.setVgap(2);
      lm.setHgap(3);
      this.setLayout(lm);
      this.enter = new Button(this.l.get("loginform.enter"));
      this.enter.setFont(this.lf.font_bold);
      this.enter.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ButtonPanel.this.lf.callLogin();
         }
      });
      this.cancel = new Button(this.l.get("loginform.cancel", "t", this.lf.autologin.timeout));
      this.cancel.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ButtonPanel.this.lf.setAutoLogin(false);
         }
      });
      this.settings = new SettingsButton(this.lf);
      this.support = new SupportButton(this.lf);
      this.add("Center", this.enter);
      this.add("East", this.settings);
      if (this.lf.autologin.enabled) {
         this.add("South", this.cancel);
      } else {
         this.add("South", this.support);
      }

   }

   void toggleEnterButton(boolean play) {
      this.enter.setLabel(this.l.get("loginform.enter" + (!play ? ".install" : "")));
   }

   void toggleSouthButton() {
      this.remove(this.cancel);
      this.add("South", this.support);
      this.validate();
   }

   protected void blockElement(Object reason) {
      this.enter.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.enter.setEnabled(true);
   }
}
