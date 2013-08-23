package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.Checkbox;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;

public class CheckBoxPanel extends BlockablePanel {
   private static final long serialVersionUID = 1808335203922301270L;
   private final LoginForm lf;
   private final Settings l;
   boolean blocked;
   Checkbox autologinbox;
   Checkbox consolebox;
   boolean console;
   Checkbox forceupdatebox;
   boolean forceupdate;

   public CheckBoxPanel(LoginForm loginform, boolean autologin_enabled, boolean console_enabled) {
      this.lf = loginform;
      this.l = this.lf.l;
      LayoutManager lm = new BoxLayout(this, 1);
      this.setLayout(lm);
      this.autologinbox = new Checkbox(this.l.get("loginform.checkbox.autologin"));
      this.autologinbox.setState(autologin_enabled);
      this.autologinbox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            if (CheckBoxPanel.this.blocked) {
               CheckBoxPanel.this.autologinbox.setState(!newstate);
            } else {
               CheckBoxPanel.this.lf.setAutoLogin(newstate);
            }

         }
      });
      this.autologinbox.setVisible(false);
      this.consolebox = new Checkbox(this.l.get("loginform.checkbox.console"));
      this.consolebox.setState(this.console = console_enabled);
      this.consolebox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            if (CheckBoxPanel.this.blocked) {
               CheckBoxPanel.this.consolebox.setState(!newstate);
            } else {
               CheckBoxPanel.this.console = newstate;
            }

         }
      });
      this.forceupdatebox = new Checkbox(this.l.get("loginform.checkbox.forceupdate"));
      this.forceupdatebox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean newstate = e.getStateChange() == 1;
            if (CheckBoxPanel.this.blocked) {
               CheckBoxPanel.this.forceupdatebox.setState(!newstate);
            } else {
               CheckBoxPanel.this.forceupdate = newstate;
            }

         }
      });
      this.add(this.autologinbox);
      this.add(this.consolebox);
      this.add(this.forceupdatebox);
   }

   void uncheckAutologin() {
      this.autologinbox.setState(false);
   }

   protected void blockElement(Object reason) {
      this.blocked = true;
   }

   protected void unblockElement(Object reason) {
      this.blocked = false;
   }
}
