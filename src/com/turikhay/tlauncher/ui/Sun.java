package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.timer.TimerTask;
import com.turikhay.tlauncher.util.U;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

public class Sun {
   final TLauncherFrame f;
   final Sun sun = this;
   private Image image;
   private int width;
   private int height;
   private int x;
   private int y;
   private double percent;
   private Color bgcolor;
   private boolean cancelled;

   public Sun(MainContainer mc) {
      this.f = mc.f;
      this.image = mc.f.sun;
      this.bgcolor = mc.f.bgcolor;
      this.width = this.image.getWidth((ImageObserver)null);
      this.height = this.image.getHeight((ImageObserver)null);
      this.percent = U.doubleRandom();
      if (this.percent < 0.0D) {
         this.percent += 0.5D;
      } else {
         this.percent -= 0.5D;
      }

      if (this.allowed()) {
         this.addTask();
      }

   }

   private void addTask() {
      this.f.ti.add("sun", new TimerTask() {
         public void run() {
            Sun var10000 = Sun.this;
            var10000.percent = var10000.percent - 0.001D;
            Sun.this.f.mc.repaint();
         }

         public boolean isRepeating() {
            return true;
         }

         public int getTicks() {
            return 1;
         }
      });
   }

   private void recalculateCoordinates(int w, int h) {
      if (this.percent < -1.0D) {
         this.percent = 1.0D;
      }

      this.x = (int)((double)w * this.percent);
      this.y = h - 360;
   }

   public void onPaint(int w, int h, Graphics g) {
      if (!this.cancelled) {
         this.recalculateCoordinates(w, h);
         g.drawImage(this.image, this.x, this.y, (ImageObserver)null);
         Color oldcolor = g.getColor();
         g.setColor(this.bgcolor);
         g.fillRect(this.x + this.width, this.y, this.width, this.height);
         g.setColor(oldcolor);
      }
   }

   public void suspend() {
      U.log("The sun is suspended.");
      this.cancelled = true;
      this.f.ti.remove("sun");
   }

   public boolean cancelled() {
      return this.cancelled;
   }

   public void resume() {
      if (this.allowed()) {
         U.log("The sun is resumed.");
         this.cancelled = false;
         this.addTask();
      }
   }

   public void cancel() {
      this.f.t.settings.set("gui.sun", false);
      this.suspend();
      U.log("The sun is stopped.");
   }

   public void allow() {
      this.f.t.settings.set("gui.sun", true);
      this.resume();
   }

   private boolean allowed() {
      return this.f.global.getBoolean("gui.sun");
   }
}
