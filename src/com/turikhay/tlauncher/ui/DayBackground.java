package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;
import java.awt.Graphics2D;

public class DayBackground extends LightBackground implements AnimatedBackground {
   public final double MIN = -0.3D;
   public final double MAX = 1.3D;
   private boolean started;
   private boolean redraw;

   public DayBackground(DecoratedPanel comp, double loc, boolean start) {
      super(comp, loc);
      if (start) {
         this.start();
      }

   }

   public void start() {
      if (!this.started) {
         TLauncher.getInstance().getSettings().set("gui.sun", true);
         this.started = true;
         this.redraw = true;
         AsyncThread.execute(new Runnable() {
            public void run() {
               while(DayBackground.this.started) {
                  DayBackground.this.tick();
               }

            }
         });
         U.log("Sun started");
      }
   }

   public void suspend() {
      this.started = false;
      U.log("Sun suspended");
   }

   public void stop() {
      TLauncher.getInstance().getSettings().set("gui.sun", false);
      this.started = false;
      U.log("Sun stopped");
   }

   public boolean getState() {
      return TLauncher.getInstance().getSettings().getBoolean("gui.sun");
   }

   private void tick() {
      if (this.sunLocation <= -0.3D) {
         this.sunLocation = 1.3D;
      } else {
         this.sunLocation -= 0.001D;
      }

      long start = System.nanoTime();
      this.comp.repaint();
      long end = System.nanoTime();
      long diff = end - start;
      if (diff > 500000L) {
         U.log("Sun is lagging (" + diff + " ns > 500000 ns).");
      }

      U.sleepFor(1000L);
   }

   public void draw(Graphics2D g, boolean force) {
      if (this.redraw) {
         force = true;
         this.redraw = false;
      }

      super.draw(g, force);
   }
}
