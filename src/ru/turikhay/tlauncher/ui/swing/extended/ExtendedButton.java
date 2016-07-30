package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.ImageIcon;

public class ExtendedButton extends JButton {
   public ExtendedButton() {
      this.init();
   }

   public ExtendedButton(String text) {
      super(text);
      this.init();
   }

   public void setIcon(Icon icon) {
      super.setIcon(icon);
      if (icon instanceof ImageIcon) {
         super.setDisabledIcon(((ImageIcon)icon).getDisabledInstance());
      }

   }

   private void init() {
      this.setFont(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
      this.setOpaque(false);
      this.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Component parent = this.findRootParent(ExtendedButton.this.getParent());
            if (parent != null) {
               parent.requestFocusInWindow();
            }

         }

         private Component findRootParent(Component comp) {
            return comp == null ? null : (comp.getParent() == null ? comp : this.findRootParent(comp.getParent()));
         }
      });
   }
}
