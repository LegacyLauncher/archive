package ru.turikhay.tlauncher.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

public class Del extends ExtendedPanel {
   private int size;
   private int aligment;
   private Color color;

   public Del(int size, int aligment, Color color) {
      this.size = SwingUtil.magnify(size);
      this.aligment = aligment;
      this.color = color;
   }

   public void paint(Graphics g) {
      g.setColor(this.color);
      switch(this.aligment) {
      case -1:
         g.fillRect(0, 0, this.getWidth(), this.size);
         break;
      case 0:
         g.fillRect(0, this.getHeight() / 2 - this.size, this.getWidth(), this.size);
         break;
      case 1:
         g.fillRect(0, this.getHeight() - this.size, this.getWidth(), this.size);
      }

   }
}
