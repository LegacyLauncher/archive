package ru.turikhay.tlauncher.ui.background;

import java.awt.Color;
import java.awt.Graphics;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;

public class BackgroundCover extends ExtendedPanel implements ResizeableComponent {
   private static final long serialVersionUID = -1801217638400760969L;
   private static final double opacityStep = 0.01D;
   private static final int timeFrame = 5;
   private final BackgroundHolder parent;
   private final Object animationLock;
   private double opacity;
   private Color opacityColor;
   private Color color;

   BackgroundCover(BackgroundHolder parent, Color opacityColor, double opacity) {
      if (parent == null) {
         throw new NullPointerException();
      } else {
         this.parent = parent;
         this.setColor(opacityColor, false);
         this.setOpacity(opacity, false);
         this.animationLock = new Object();
      }
   }

   BackgroundCover(BackgroundHolder parent) {
      this(parent, Color.white, 0.0D);
   }

   public void makeCover(boolean animate) {
      synchronized(this.animationLock) {
         if (animate) {
            while(this.opacity < 1.0D) {
               this.setOpacity(this.opacity + 0.01D, true);
               U.sleepFor(5L);
            }
         }

         this.setOpacity(1.0D, true);
      }
   }

   public void makeCover() {
      this.makeCover(true);
   }

   public void removeCover(boolean animate) {
      synchronized(this.animationLock) {
         if (animate) {
            while(this.opacity > 0.0D) {
               this.setOpacity(this.opacity - 0.01D, true);
               U.sleepFor(5L);
            }
         }

         this.setOpacity(0.0D, true);
      }
   }

   public void removeCover() {
      this.removeCover(true);
   }

   public boolean isCovered() {
      return this.opacity == 1.0D;
   }

   public void toggleCover(boolean animate) {
      if (this.isCovered()) {
         this.removeCover(animate);
      } else {
         this.makeCover(animate);
      }

   }

   public void paint(Graphics g) {
      g.setColor(this.opacityColor);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
   }

   public double getOpacity() {
      return this.opacity;
   }

   public void setOpacity(double opacity, boolean repaint) {
      if (opacity < 0.0D) {
         opacity = 0.0D;
      } else if (opacity > 1.0D) {
         opacity = 1.0D;
      }

      this.opacity = opacity;
      this.opacityColor = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)(255.0D * opacity));
      if (repaint) {
         this.repaint();
      }

   }

   public Color getColor() {
      return this.color;
   }

   public void setColor(Color color, boolean repaint) {
      if (color == null) {
         throw new NullPointerException();
      } else {
         this.color = color;
         if (repaint) {
            this.repaint();
         }

      }
   }

   public void onResize() {
      this.setSize(this.parent.getWidth(), this.parent.getHeight());
   }
}
