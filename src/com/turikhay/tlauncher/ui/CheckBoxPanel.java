package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.settings.Settings;
import java.awt.Checkbox;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;

public class CheckBoxPanel extends BlockablePanel {
   private static final long serialVersionUID = 1808335203922301270L;
   private final Settings l;
   boolean blocked;
   Checkbox consolebox;
   boolean console;
   Checkbox forceupdatebox;
   boolean forceupdate;

   public CheckBoxPanel(LoginForm lf, boolean enabled) {
      this.l = lf.l;
      LayoutManager lm = new BoxLayout(this, 1);
      this.setLayout(lm);
      this.consolebox = new Checkbox(this.l.get("loginform.checkbox.console"));
      this.consolebox.setState(this.console = enabled);
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
      this.add(this.consolebox);
      this.add(this.forceupdatebox);
   }

   protected void blockElement(Object reason) {
      this.blocked = true;
   }

   protected void unblockElement(Object reason) {
      this.blocked = false;
   }
}
