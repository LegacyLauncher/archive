package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonPanel extends BlockablePanel {
   private static final long serialVersionUID = 5873050319650201358L;
   public static final int ENTERBUTTON_INSTALL = -1;
   public static final int ENTERBUTTON_PLAY = 0;
   public static final int ENTERBUTTON_REINSTALL = 1;
   final LoginForm lf;
   final Settings l;
   public final LocalizableButton enter;
   public final LocalizableButton cancel;
   public final AdditionalButtonsPanel addbuttons;

   ButtonPanel(LoginForm loginform) {
      this.lf = loginform;
      this.l = this.lf.l;
      BorderLayout lm = new BorderLayout();
      lm.setVgap(2);
      lm.setHgap(3);
      this.setLayout(lm);
      this.enter = new LocalizableButton("loginform.enter");
      this.enter.setFont(this.lf.font_bold);
      this.enter.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ButtonPanel.this.lf.callLogin();
         }
      });
      this.cancel = new LocalizableButton("loginform.cancel", "t", this.lf.autologin.timeout);
      this.cancel.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ButtonPanel.this.lf.setAutoLogin(false);
         }
      });
      this.addbuttons = new AdditionalButtonsPanel(this);
      this.add("Center", this.enter);
      if (this.lf.autologin.enabled) {
         this.add("South", this.cancel);
      } else {
         this.add("South", this.addbuttons);
      }

   }

   void updateEnterButton() {
      if (this.lf.versionchoice.selected != null) {
         boolean play = this.lf.versionchoice.selected.isInstalled();
         boolean force = this.lf.checkbox.getForceUpdate();
         int status = true;
         byte status;
         if (play) {
            if (force) {
               status = 1;
            } else {
               status = 0;
            }
         } else {
            status = -1;
         }

         String s = ".";
         switch(status) {
         case -1:
            s = s + "install";
            break;
         case 0:
            s = "";
            break;
         case 1:
            s = s + "reinstall";
            break;
         default:
            throw new IllegalArgumentException("Status is invalid! Use ButtonPanel.ENTERBUTTON_* variables.");
         }

         this.enter.setLabel("loginform.enter" + s);
      }
   }

   void toggleSouthButton() {
      this.remove(this.cancel);
      this.add("South", this.addbuttons);
      this.validate();
   }

   protected void blockElement(Object reason) {
      this.enter.setEnabled(false);
      this.addbuttons.blockElement(reason);
   }

   protected void unblockElement(Object reason) {
      this.enter.setEnabled(true);
      this.addbuttons.unblockElement(reason);
   }
}
