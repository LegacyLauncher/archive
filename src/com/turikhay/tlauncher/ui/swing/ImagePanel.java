package com.turikhay.tlauncher.ui.swing;

import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import com.turikhay.util.U;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;

public class ImagePanel extends ExtendedPanel {
   private static final long serialVersionUID = 1L;
   public static final float DEFAULT_ACTIVE_OPACITY = 1.0F;
   public static final float DEFAULT_NON_ACTIVE_OPACITY = 0.75F;
   private final Object animationLock;
   private Image image;
   private float activeOpacity;
   private float nonActiveOpacity;
   private boolean antiAlias;
   private int timeFrame;
   private float opacity;
   private boolean hover;
   private boolean shown;
   private boolean animating;

   public ImagePanel(String image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias) {
      this(ImageCache.getImage(image), activeOpacity, nonActiveOpacity, shown, antiAlias);
   }

   protected ImagePanel(Image image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias) {
      this.animationLock = new Object();
      this.setImage(image);
      this.setActiveOpacity(activeOpacity);
      this.setNonActiveOpacity(nonActiveOpacity);
      this.setAntiAlias(antiAlias);
      this.shown = shown;
      this.opacity = shown ? nonActiveOpacity : 0.0F;
      this.timeFrame = 10;
      this.setBackground(new Color(0, 0, 0, 0));
      this.addMouseListenerOriginally(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            ImagePanel.this.onClick();
         }

         public void mouseEntered(MouseEvent e) {
            ImagePanel.this.onMouseEntered();
         }

         public void mouseExited(MouseEvent e) {
            ImagePanel.this.onMouseExited();
         }
      });
   }

   void setImage(Image image, boolean resetSize) {
      if (image == null) {
         throw new NullPointerException();
      } else {
         synchronized(this.animationLock) {
            this.image = image;
            this.setSize(image.getWidth((ImageObserver)null), image.getHeight((ImageObserver)null));
         }
      }
   }

   void setImage(Image image) {
      this.setImage(image, true);
   }

   void setActiveOpacity(float opacity) {
      if (!(opacity > 1.0F) && !(opacity < 0.0F)) {
         this.activeOpacity = opacity;
      } else {
         throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: " + opacity + ") <= 1.0F");
      }
   }

   void setNonActiveOpacity(float opacity) {
      if (!(opacity > 1.0F) && !(opacity < 0.0F)) {
         this.nonActiveOpacity = opacity;
      } else {
         throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: " + opacity + ") <= 1.0F");
      }
   }

   void setAntiAlias(boolean set) {
      this.antiAlias = set;
   }

   public void paintComponent(Graphics g0) {
      Graphics2D g = (Graphics2D)g0;
      Composite oldComp = g.getComposite();
      if (this.antiAlias) {
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      }

      g.setComposite(AlphaComposite.getInstance(3, this.opacity));
      g.drawImage(this.image, 0, 0, this.getWidth(), this.getHeight(), (ImageObserver)null);
      g.setComposite(oldComp);
   }

   public void show() {
      if (!this.shown) {
         this.shown = true;
         synchronized(this.animationLock) {
            this.animating = true;
            this.setVisible(true);
            this.opacity = 0.0F;
            float selectedOpacity = this.hover ? this.activeOpacity : this.nonActiveOpacity;

            while(this.opacity < selectedOpacity) {
               this.opacity += 0.01F;
               if (this.opacity > selectedOpacity) {
                  this.opacity = selectedOpacity;
               }

               this.repaint();
               U.sleepFor((long)this.timeFrame);
            }

            this.animating = false;
         }
      }
   }

   public void hide() {
      if (this.shown) {
         this.shown = false;
         synchronized(this.animationLock) {
            this.animating = true;

            while(this.opacity > 0.0F) {
               this.opacity -= 0.01F;
               if (this.opacity < 0.0F) {
                  this.opacity = 0.0F;
               }

               this.repaint();
               U.sleepFor((long)this.timeFrame);
            }

            this.setVisible(false);
            this.animating = false;
         }
      }
   }

   protected boolean onClick() {
      return this.shown;
   }

   void onMouseEntered() {
      this.hover = true;
      if (!this.animating && this.shown) {
         this.opacity = this.activeOpacity;
         this.repaint();
      }
   }

   void onMouseExited() {
      this.hover = false;
      if (!this.animating && this.shown) {
         this.opacity = this.nonActiveOpacity;
         this.repaint();
      }
   }
}
