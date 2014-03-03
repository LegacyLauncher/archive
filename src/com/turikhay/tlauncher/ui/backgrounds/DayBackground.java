package com.turikhay.tlauncher.ui.backgrounds;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;
import java.awt.Graphics2D;

public class DayBackground extends LightBackground {
   private static final long serialVersionUID = -3722426754002999260L;
   public final double MIN;
   public final double MAX;
   private final int TICK;
   private boolean started;
   private boolean redraw;

   public DayBackground(MainPane main) {
      this(main, U.doubleRandom());
   }

   public DayBackground(MainPane main, double loc) {
      super(main, loc);
      this.MIN = -0.5D;
      this.MAX = 1.5D;
      this.TICK = 100;
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
         log("Sun has been started");
      }
   }

   public void suspend() {
      this.started = false;
      log("Sun has been suspended");
   }

   public void stop() {
      TLauncher.getInstance().getSettings().set("gui.sun", false);
      this.started = false;
      log("Sun has been stopped");
   }

   private void tick() {
      if (this.isVisible()) {
         if (this.sunLocation <= -0.5D) {
            this.sunLocation = 1.5D;
         } else {
            this.sunLocation -= 0.001D;
         }

         long start = System.nanoTime();
         this.repaint();
         long end = System.nanoTime();
         long diff = end - start;
         if (diff > 1000000L) {
            log("Sun is probably lagging (" + diff + " ns > 1000000 ns).");
         }

         U.sleepFor(100L);
      }
   }

   public void draw(Graphics2D g, boolean force) {
      if (this.redraw) {
         force = true;
         this.redraw = false;
      }

      super.draw(g, force);
   }

   private static void log(Object... o) {
      U.log("[DayBackground]", o);
   }
}
