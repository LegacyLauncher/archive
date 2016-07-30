package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import javax.swing.JFrame;
import ru.turikhay.util.U;

public class ExtendedFrame extends JFrame {
   public void showAndWait() {
      this.showAtCenter();

      while(this.isDisplayable()) {
         U.sleepFor(100L);
      }

   }

   public void showAtCenter() {
      this.setVisible(true);
      this.setLocationRelativeTo((Component)null);
   }
}
