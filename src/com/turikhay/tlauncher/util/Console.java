package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.ui.ConsoleFrame;
import java.awt.Dimension;
import java.awt.Point;

public class Console {
   private final ConsoleFrame cf;
   private final Console instance;
   private boolean killed;

   public Console(String name) {
      this.instance = this;
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

   public void plog(Object obj) {
      this.check();
      this.cf.print("" + obj);
   }

   public void log(Object obj) {
      this.check();
      this.cf.println("" + obj);
   }

   public void log(Object obj0, Object obj1) {
      this.check();
      this.cf.println(obj0 + " " + obj1);
   }

   public void log(Object obj, Throwable e) {
      this.check();
      this.cf.println("" + obj);
      this.cf.println(U.stackTrace(e));
   }

   public void log(Throwable e) {
      this.check();
      this.cf.println(U.stackTrace(e));
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
      }
   }
}
