package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.util.U;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
   private static final long serialVersionUID = 1L;
   public static final float DEFAULT_ACTIVE_OPACITY = 1.0F;
   public static final float DEFAULT_NON_ACTIVE_OPACITY = 0.75F;
   protected Object animationLock;
   protected Image image;
   protected float activeOpacity;
   protected float nonActiveOpacity;
   protected boolean antiAlias;
   protected int timeFrame;
   protected float opacity;
   protected boolean hover;
   protected boolean shown;
   protected boolean animating;

   public ImagePanel(String image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias) {
      this(ImageCache.getImage(image), activeOpacity, nonActiveOpacity, shown, antiAlias);
   }

   public ImagePanel(Image image, float activeOpacity, float nonActiveOpacity, boolean shown, boolean antiAlias) {
      this.animationLock = new Object();
      this.setImage(image);
      this.setActiveOpacity(activeOpacity);
      this.setNonActiveOpacity(nonActiveOpacity);
      this.setAntiAlias(antiAlias);
      this.shown = shown;
      this.opacity = shown ? nonActiveOpacity : 0.0F;
      this.timeFrame = 10;
      this.setBackground(new Color(0, 0, 0, 0));
      this.setOpaque(false);
      this.addMouseListener(new MouseListener() {
         public void mouseClicked(MouseEvent e) {
            ImagePanel.this.onClick();
         }

         public void mousePressed(MouseEvent e) {
         }

         public void mouseReleased(MouseEvent e) {
         }

         public void mouseEntered(MouseEvent e) {
            ImagePanel.this.onMouseEntered();
         }

         public void mouseExited(MouseEvent e) {
            ImagePanel.this.onMouseExited();
         }
      });
   }

   public void setImage(Image image, boolean resetSize) {
      if (image == null) {
         throw new NullPointerException();
      } else {
         synchronized(this.animationLock) {
            this.image = image;
            this.setSize(image.getWidth((ImageObserver)null), image.getHeight((ImageObserver)null));
         }
      }
   }

   public void setImage(Image image) {
      this.setImage(image, true);
   }

   public void setActiveOpacity(float opacity) {
      if (!(opacity > 1.0F) && !(opacity < 0.0F)) {
         this.activeOpacity = opacity;
      } else {
         throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: " + opacity + ") <= 1.0F");
      }
   }

   public void setNonActiveOpacity(float opacity) {
      if (!(opacity > 1.0F) && !(opacity < 0.0F)) {
         this.nonActiveOpacity = opacity;
      } else {
         throw new IllegalArgumentException("Invalid opacity! Condition: 0.0F <= opacity (got: " + opacity + ") <= 1.0F");
      }
   }

   public void setAntiAlias(boolean set) {
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

   protected void onMouseEntered() {
      this.hover = true;
      if (!this.animating && this.shown) {
         this.opacity = this.activeOpacity;
         this.repaint();
      }
   }

   protected void onMouseExited() {
      this.hover = false;
      if (!this.animating && this.shown) {
         this.opacity = this.nonActiveOpacity;
         this.repaint();
      }
   }
}
