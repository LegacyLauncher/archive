package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.ui.ConsoleFrame;
import java.awt.Dimension;
import java.awt.Point;

public class Console {
   private final ConsoleFrame cf;
   private final Console instance;
   private String del;
   private boolean killed;

   public Console(String name) {
      this.instance = this;
      this.del = null;
      this.cf = new ConsoleFrame(name);
   }

   public Console(String name, boolean show) {
      this(name);
      this.cf.setVisible(show);
   }

   public void show() {
      this.check();
      this.cf.setVisible(true);
      this.cf.toFront();
   }

   public void hide() {
      this.check();
      this.cf.setVisible(false);
   }

   public void kill() {
      this.check();
      this.cf.setVisible(false);
      this.killed = true;
   }

   public void killIn(long millis) {
      AsyncThread.execute(new Runnable() {
         public void run() {
            Console.this.instance.kill();
         }
      }, millis);
   }

   public boolean isKilled() {
      this.check();
      return this.killed;
   }

   public boolean isHidden() {
      this.check();
      return !this.cf.isShowing();
   }

   public void log(Object obj) {
      this.check();
      this.cf.print(this.del + obj);
   }

   public void log(Object obj0, Object obj1) {
      this.check();
      this.cf.print(this.del + obj0 + " " + obj1);
   }

   public void log(Object obj, Throwable e) {
      this.check();
      this.cf.println(this.del + obj);
      this.cf.print(U.stackTrace(e));
   }

   public void log(Throwable e) {
      this.check();
      this.cf.print(this.del + U.stackTrace(e));
   }

   public Point getPositionPoint() {
      this.check();
      return this.cf.getLocation();
   }

   public int[] getPosition() {
      this.check();
      Point p = this.getPositionPoint();
      return new int[]{p.x, p.y};
   }

   public Dimension getDimension() {
      this.check();
      return this.cf.getSize();
   }

   public int[] getSize() {
      this.check();
      Dimension d = this.getDimension();
      return new int[]{d.width, d.height};
   }

   private void check() {
      if (this.killed) {
         throw new IllegalStateException("Console is already killed!");
      } else {
         if (this.del == null) {
            this.del = "";
         } else if (this.del == "") {
            this.del = "\n";
         }

      }
   }
}
