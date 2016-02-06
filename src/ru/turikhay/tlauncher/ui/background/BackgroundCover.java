package ru.turikhay.tlauncher.ui.background;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.U;

public class BackgroundCover extends BorderPanel implements ResizeableComponent {
   private final BackgroundHolder parent;
   private final Object animationLock;
   private float opacity;
   private Color opacityColor;
   private Color color;

   BackgroundCover(BackgroundHolder parent, Color opacityColor, float opacity) {
      if (parent == null) {
         throw new NullPointerException();
      } else {
         this.setOpaque(false);
         this.parent = parent;
         this.setColor(opacityColor, false);
         this.setBgOpacity(opacity, false);
         this.animationLock = new Object();
         this.setInsets(new MagnifiedInsets(5, 5, 5, 5));
         this.setNorth(this.newLoadingLabel());
         this.setSouth(this.newLoadingLabel());
      }
   }

   private LocalizableLabel newLoadingLabel() {
      LocalizableLabel label = new LocalizableLabel("background.loading");
      label.setForeground(new Color(255 - this.opacityColor.getRed(), 255 - this.opacityColor.getGreen(), 255 - this.opacityColor.getBlue()));
      label.setHorizontalAlignment(0);
      return label;
   }

   BackgroundCover(BackgroundHolder parent) {
      this(parent, Color.black, 0.0F);
   }

   public void makeCover(boolean animate) {
      synchronized(this.animationLock) {
         if (animate) {
            while(this.opacity < 1.0F) {
               this.setBgOpacity(this.opacity + 0.01F, true);
               U.sleepFor(5L);
            }
         }

         this.setBgOpacity(1.0F, true);
      }
   }

   public void removeCover(boolean animate) {
      synchronized(this.animationLock) {
         if (animate) {
            while(this.opacity > 0.0F) {
               this.setBgOpacity(this.opacity - 0.01F, true);
               U.sleepFor(5L);
            }
         }

         this.setBgOpacity(0.0F, true);
      }

      if (animate) {
         this.removeAll();
      }

   }

   public void paint(Graphics g0) {
      g0.setColor(this.opacityColor);
      g0.fillRect(0, 0, this.getWidth(), this.getHeight());
      Graphics2D g = (Graphics2D)g0;
      Composite oldComp = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(3, this.opacity));
      super.paint(g0);
      g.setComposite(oldComp);
   }

   public void setBgOpacity(float opacity, boolean repaint) {
      if (opacity < 0.0F) {
         opacity = 0.0F;
      } else if (opacity > 1.0F) {
         opacity = 1.0F;
      }

      this.opacity = opacity;
      this.opacityColor = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)(255.0F * opacity));
      if (repaint) {
         this.repaint();
      }

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
