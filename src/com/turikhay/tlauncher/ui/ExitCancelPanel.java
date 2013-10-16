package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;

public class ExitCancelPanel extends Panel {
   private static final long serialVersionUID = -1998881418330942647L;
   LocalizableLabel label;
   LocalizableButton button;
   Font font = new Font("", 1, 12);

   ExitCancelPanel(final ConsoleFrame cf) {
      this.setLayout(new BoxLayout(this, 1));
      this.setBackground(Color.black);
      this.setForeground(Color.white);
      this.label = new LocalizableLabel(1);
      this.button = new LocalizableButton("console.close.cancel");
      this.label.setFont(this.font);
      this.button.setBackground(Color.black);
      this.button.setForeground(Color.white);
      this.button.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            cf.cancelHiding();
         }
      });
      this.add("Center", this.label);
      this.add("South", this.button);
   }

   void setTimeout(int timeout) {
      this.label.setText("console.close.text", "s", timeout);
   }
}
