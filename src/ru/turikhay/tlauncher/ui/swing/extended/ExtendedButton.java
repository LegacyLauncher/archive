package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class ExtendedButton extends JButton {
   private static final long serialVersionUID = -2009736184875993130L;

   protected ExtendedButton() {
      this.init();
   }

   public ExtendedButton(Icon icon) {
      super(icon);
      this.init();
   }

   protected ExtendedButton(String text) {
      super(text);
      this.init();
   }

   public ExtendedButton(Action a) {
      super(a);
      this.init();
   }

   public ExtendedButton(String text, Icon icon) {
      super(text, icon);
      this.init();
   }

   private void init() {
      this.setFont(this.getFont().deriveFont(12.0F));
      this.setOpaque(false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Component parent = this.findRootParent(ExtendedButton.this.getParent());
            if (parent != null) {
               parent.requestFocusInWindow();
            }
         }

         private Component findRootParent(Component comp) {
            if (comp == null) {
               return null;
            } else {
               return comp.getParent() == null ? comp : this.findRootParent(comp.getParent());
            }
         }
      });
   }
}
