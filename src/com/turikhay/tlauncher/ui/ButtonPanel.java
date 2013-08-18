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
      this.settings = new SettingsButton(this.lf);
      this.support = new SupportButton(this.lf);
      this.add("Center", this.enter);
      this.add("East", this.settings);
      this.add("South", this.support);
   }

   void toggleEnterButton(boolean play) {
      this.enter.setLabel(this.l.get("loginform.enter" + (!play ? ".install" : "")));
   }

   protected void blockElement(Object reason) {
      this.enter.setEnabled(false);
   }

   protected void unblockElement(Object reason) {
      this.enter.setEnabled(true);
   }
}
