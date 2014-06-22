package ru.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class ServicePanel extends ExtendedPanel implements ResizeableComponent {
   private static final long serialVersionUID = -3973551999471811629L;
   private final MainPane pane;
   private final Image helper;
   private int width;
   private int height;
   private int y;
   private float opacity;
   private ServicePanel.ServicePanelThread thread;
   private boolean mouseIn;

   ServicePanel(MainPane pane) {
      this.pane = pane;
      this.helper = ImageCache.getImage("helper.png", false);
      if (this.helper != null) {
         this.width = this.helper.getWidth((ImageObserver)null);
         this.height = this.helper.getHeight((ImageObserver)null);
         pane.add(this);
         this.setSize(this.width, this.height);
         this.opacity = 0.1F;
         this.y = 0;
         this.thread = new ServicePanel.ServicePanelThread();
         pane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
               ServicePanel.this.onResize();
            }
         });
         this.addMouseListenerOriginally(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
               ServicePanel.this.mouseIn = true;
               ServicePanel.this.thread.iterate();
            }

            public void mouseExited(MouseEvent e) {
               ServicePanel.this.mouseIn = false;
            }
         });
      }
   }

   public void paint(Graphics g0) {
      if (this.thread.isIterating()) {
         Graphics2D g = (Graphics2D)g0;
         g.setComposite(AlphaComposite.getInstance(3, this.opacity));
         g.drawImage(this.helper, this.getWidth() / 2 - this.width / 2, this.getHeight() - this.y, (ImageObserver)null);
      }
   }

   public void onResize() {
      this.setLocation(this.pane.getWidth() - this.getWidth(), this.pane.getHeight() - this.getHeight());
   }

   class ServicePanelThread extends LoopedThread {
      private static final int PIXEL_STEP = 5;
      private static final int TIMEFRAME = 25;
      private static final float OPACITY_STEP = 0.05F;

      ServicePanelThread() {
         super("ServicePanel");
         this.startAndWait();
      }

      protected void iterateOnce() {
         int timeout = 10;

         while(true) {
            --timeout;
            if (timeout <= 0) {
               ServicePanel.this.y = 1;

               while(ServicePanel.this.y > 0) {
                  while(ServicePanel.this.mouseIn) {
                     this.onIn();
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

      private void onIn() {
         ServicePanel var10000;
         if (ServicePanel.this.y < ServicePanel.this.getHeight()) {
            var10000 = ServicePanel.this;
            var10000.y = var10000.y + 5;
         }

         if (ServicePanel.this.y > ServicePanel.this.getHeight()) {
            ServicePanel.this.y = ServicePanel.this.getHeight();
         }

         if ((double)ServicePanel.this.opacity < 0.9D) {
            var10000 = ServicePanel.this;
            var10000.opacity = var10000.opacity + 0.05F;
         }

         if (ServicePanel.this.opacity > 1.0F) {
            ServicePanel.this.opacity = 1.0F;
         }

         this.repaintSleep();
      }

      private void onOut() {
         ServicePanel var10000;
         if (ServicePanel.this.y > 0) {
            var10000 = ServicePanel.this;
            var10000.y = var10000.y - 5;
         }

         if (ServicePanel.this.y < 0) {
            ServicePanel.this.y = 0;
         }

         if ((double)ServicePanel.this.opacity > 0.0D) {
            var10000 = ServicePanel.this;
            var10000.opacity = var10000.opacity - 0.05F;
         }

         if (ServicePanel.this.opacity < 0.0F) {
            ServicePanel.this.opacity = 0.0F;
         }

         this.repaintSleep();
      }

      private void repaintSleep() {
         ServicePanel.this.repaint();
         U.sleepFor(25L);
      }
   }
}
