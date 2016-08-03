package ru.turikhay.tlauncher.ui.background;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;

final class Cover extends JComponent implements ResizeableComponent {
   private Color color;
   private float opacity;

   Cover() {
      this.setColor((Color)null);
      this.setOpacity(0.0F);
   }

   void setColor(Color color) {
      this.color = color == null ? Color.black : color;
   }

   float getOpacity() {
      return this.opacity;
   }

   void setOpacity(float opacity) {
      if (opacity < 0.0F) {
         opacity = 0.0F;
      } else if (opacity > 1.0F) {
         opacity = 1.0F;
      }

      this.opacity = opacity;
      this.repaint();
   }

   public void paint(Graphics g0) {
      Graphics2D g = (Graphics2D)g0;
      g.setComposite(AlphaComposite.getInstance(3, this.opacity));
      g.setColor(this.color);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
   }

   public void onResize() {
      if (this.getParent() != null) {
         this.setSize(this.getParent().getSize());
      }

   }
}
