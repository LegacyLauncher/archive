package ru.turikhay.tlauncher.ui.images;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Icon;

public class DisabledImageIcon implements Icon {
   private final Icon parent;
   private float disabledOpacity;
   private AlphaComposite opacityComposite;

   public DisabledImageIcon(Icon parent, float opacity) {
      if (parent == null) {
         throw new NullPointerException();
      } else {
         this.parent = parent;
         this.setDisabledOpacity(opacity);
      }
   }

   public DisabledImageIcon(Icon parent) {
      this(parent, 0.75F);
   }

   public final void setDisabledOpacity(float f) {
      if ((double)f < 0.0D) {
         throw new IllegalArgumentException();
      } else {
         this.disabledOpacity = f;
         this.opacityComposite = AlphaComposite.getInstance(3, this.disabledOpacity);
      }
   }

   public void paintIcon(Component c, Graphics g0, int x, int y) {
      Graphics2D g = (Graphics2D)g0;
      Composite oldComposite = g.getComposite();
      g.setComposite(this.opacityComposite);
      this.parent.paintIcon(c, g, x, y);
      g.setComposite(oldComposite);
   }

   public final int getIconWidth() {
      return this.parent.getIconWidth();
   }

   public final int getIconHeight() {
      return this.parent.getIconHeight();
   }
}
