package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;
import java.awt.Graphics2D;

public class DayBackground extends LightBackground implements AnimatedBackground {
   public final double MIN = -0.5D;
   public final double MAX = 1.5D;
   private boolean started;
   private boolean redraw;

   public DayBackground(DecoratedPanel comp, double loc) {
      super(comp, loc);
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

   public boolean isAllowed() {
      return TLauncher.getInstance().getSettings().getBoolean("gui.sun");
   }

   private void tick() {
      if (this.sunLocation <= -0.5D) {
         this.sunLocation = 1.5D;
      } else {
         this.sunLocation -= 0.001D;
      }

      long start = System.nanoTime();
      this.comp.repaint();
      long end = System.nanoTime();
      long diff = end - start;
      if (diff > 1000000L) {
         U.log("Sun is possibly lagging (" + diff + " ns > 1000000 ns).");
      }

      U.sleepFor(100L);
   }

   public void draw(Graphics2D g, boolean force) {
      if (this.redraw) {
         force = true;
         this.redraw = false;
      }

      super.draw(g, force);
   }
}
