package ru.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Iterator;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class ServicePanel extends ExtendedPanel implements ResizeableComponent {
   private final Image idleImage;
   private final ArrayList animaImages;
   private Image currentImage;
   private final MainPane pane;
   private int y;
   private float opacity;
   private ServicePanel.ServicePanelThread thread;
   private boolean mouseIn;
   private boolean mouseClicked;
   private boolean shown;
   private long lastCall;

   private static Image load(String url) {
      return Images.getImage(url);
   }

   ServicePanel(MainPane pane) {
      this.pane = pane;
      this.idleImage = load("heavy-idle.png");
      this.animaImages = new ArrayList(3);
      this.animaImages.add(load("heavy-cross0.png"));
      this.animaImages.add(load("heavy-cross1.png"));
      this.animaImages.add(load("heavy-raspidoreno.png"));
      pane.add(this);
      this.opacity = 0.1F;
      this.y = 0;
      this.thread = new ServicePanel.ServicePanelThread();
      pane.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            ServicePanel.this.onResize();
         }
      });
      this.addMouseListenerOriginally(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            ServicePanel.this.mouseClicked = true;
         }

         public void mouseEntered(MouseEvent e) {
            ServicePanel.this.mouseIn = true;
            ServicePanel.this.thread.iterate();
         }

         public void mouseExited(MouseEvent e) {
            ServicePanel.this.mouseClicked = false;
            ServicePanel.this.mouseIn = false;
         }
      });
      this.set(this.idleImage);
   }

   private void set(Image img) {
      this.currentImage = img;
      if (img != null) {
         this.setSize(img.getWidth((ImageObserver)null), img.getHeight((ImageObserver)null));
      }

   }

   public void paint(Graphics g0) {
      if (this.thread.isIterating()) {
         Image image = this.currentImage;
         if (image != null) {
            Graphics2D g = (Graphics2D)g0;
            g.setComposite(AlphaComposite.getInstance(3, this.opacity));
            g.drawImage(image, this.getWidth() / 2 - image.getWidth((ImageObserver)null) / 2, this.getHeight() - this.y, (ImageObserver)null);
         }
      }

   }

   public void onResize() {
      this.setLocation(this.pane.getWidth() - this.getWidth(), this.pane.getHeight() - this.getHeight());
   }

   class ServicePanelThread extends LoopedThread {
      private static final int PIXEL_STEP = 5;
      private static final int TIMEFRAME = 15;
      private static final float OPACITY_STEP = 0.05F;

      ServicePanelThread() {
         super("ServicePanel");
         this.startAndWait();
      }

      protected void iterateOnce() {
         if (!ServicePanel.this.shown) {
            int timeout = 5;

            while(true) {
               --timeout;
               if (timeout <= 0) {
                  ServicePanel.this.shown = true;
                  ServicePanel.this.y = 1;
                  ServicePanel.this.set(ServicePanel.this.idleImage);

                  while(ServicePanel.this.y > 0) {
                     while(ServicePanel.this.mouseIn) {
                        this.onIn();
                        if (ServicePanel.this.currentImage == null) {
                           return;
                        }
                     }

                     while(!ServicePanel.this.mouseIn) {
                        this.onOut();
                        if (ServicePanel.this.y == 0) {
                           return;
                        }
                     }
                  }

                  return;
               }

               if (!ServicePanel.this.mouseIn) {
                  return;
               }

               U.sleepFor(1000L);
            }
         }
      }

      private void onIn() {
         if (ServicePanel.this.y < ServicePanel.this.getHeight()) {
            ServicePanel.this.y = ServicePanel.this.y + 5;
         }

         if (ServicePanel.this.y > ServicePanel.this.getHeight()) {
            ServicePanel.this.y = ServicePanel.this.getHeight();
         }

         if ((double)ServicePanel.this.opacity < 0.9D) {
            ServicePanel.this.opacity = ServicePanel.this.opacity + 0.05F;
         }

         if (ServicePanel.this.opacity > 1.0F) {
            ServicePanel.this.opacity = 1.0F;
         }

         if (ServicePanel.this.y == ServicePanel.this.getHeight() && ServicePanel.this.mouseClicked) {
            Iterator var2 = ServicePanel.this.animaImages.iterator();

            while(var2.hasNext()) {
               BufferedImage frame = (BufferedImage)var2.next();
               ServicePanel.this.set(frame);
               ServicePanel.this.repaint();
               U.sleepFor(100L);
            }

            ServicePanel.this.set((Image)null);
         }

         this.repaintSleep();
      }

      private void onOut() {
         if (ServicePanel.this.y > 0) {
            ServicePanel.this.y = ServicePanel.this.y - 5;
         }

         if (ServicePanel.this.y < 0) {
            ServicePanel.this.y = 0;
         }

         if ((double)ServicePanel.this.opacity > 0.0D) {
            ServicePanel.this.opacity = ServicePanel.this.opacity - 0.05F;
         }

         if (ServicePanel.this.opacity < 0.0F) {
            ServicePanel.this.opacity = 0.0F;
         }

         this.repaintSleep();
      }

      private void repaintSleep() {
         ServicePanel.this.repaint();
         U.sleepFor(15L);
      }
   }
}
