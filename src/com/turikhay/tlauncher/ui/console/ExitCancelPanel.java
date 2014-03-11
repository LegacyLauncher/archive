package com.turikhay.tlauncher.ui.console;

import com.turikhay.tlauncher.ui.loc.LocalizableButton;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

class ExitCancelPanel extends JPanel {
   private static final long serialVersionUID = -1998881418330942647L;
   private LocalizableLabel label;
   private LocalizableButton button;
   private Font font = new Font("", 1, 12);

   ExitCancelPanel(final ConsoleFrame cf) {
      this.setLayout(new BoxLayout(this, 1));
      this.setBackground(Color.black);
      this.label = new LocalizableLabel(0);
      this.label.setForeground(Color.white);
      this.button = new LocalizableButton("console.close.cancel");
      this.label.setFont(this.font);
      this.label.setForeground(Color.white);
      this.button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            cf.cancelHiding();
         }
      });
      JPanel labelPan = new JPanel();
      JPanel buttonPan = new JPanel();
      labelPan.setBackground(Color.black);
      buttonPan.setBackground(Color.black);
      labelPan.add(this.label);
      buttonPan.add(this.button);
      this.add("Center", labelPan);
      this.add("South", buttonPan);
   }

   void setTimeout(int timeout) {
      this.label.setText("console.close.text", timeout);
   }
}
