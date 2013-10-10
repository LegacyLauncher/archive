package com.turikhay.tlauncher.util;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.ui.ConsoleFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Console {
   private static List frames = new ArrayList();
   private final GlobalSettings global;
   private final ConsoleFrame cf;
   private final Console instance;
   private String del;
   private boolean killed;
   private Console.CloseAction close;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$util$Console$CloseAction;

   public Console(String name) {
      this(TLauncher.getInstance() != null ? TLauncher.getInstance().getSettings() : null, name);
   }

   public Console(GlobalSettings global, String name) {
      this.instance = this;
      this.del = null;
      this.cf = new ConsoleFrame(name);
      this.global = global;
      this.cf.addWindowListener(new WindowListener() {
         public void windowOpened(WindowEvent e) {
         }

         public void windowClosing(WindowEvent e) {
            Console.this.saveSize();
            Console.this.onClose();
         }

         public void windowClosed(WindowEvent e) {
         }

         public void windowIconified(WindowEvent e) {
         }

         public void windowDeiconified(WindowEvent e) {
         }

         public void windowActivated(WindowEvent e) {
         }

         public void windowDeactivated(WindowEvent e) {
         }
      });
      frames.add(this.cf);
      if (global != null) {
         String prefix = "gui.console.";
         int width = global.getInteger(prefix + "width", 620);
         int height = global.getInteger(prefix + "height", 400);
         int x = global.getInteger(prefix + "x", 0);
         int y = global.getInteger(prefix + "y", 0);
         this.cf.setSize(width, height);
         this.cf.setLocation(x, y);
      }
   }

   public Console(GlobalSettings global, String name, boolean show) {
      this(global, name);
      this.cf.setVisible(show);
   }

   public Console(String name, boolean show) {
      this((GlobalSettings)null, name, show);
   }

   public void show() {
      this.check();
      this.cf.setVisible(true);
      this.cf.toFront();
      this.cf.scrollBottom();
   }

   public void hide() {
      this.check();
      this.cf.setVisible(false);
   }

   public void kill() {
      this.check();
      this.saveSize();
      this.cf.setVisible(false);
      frames.remove(this.cf);
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

   public void log(Object... obj) {
      this.check();
      this.cf.print(this.del);
      this.cf.print(U.toLog(obj));
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

   public String getOutput() {
      return this.cf.getOutput();
   }

   public void saveSize() {
      this.check();
      if (this.global != null) {
         String prefix = "gui.console.";
         int[] size = this.getSize();
         int[] position = this.getPosition();
         this.global.set(prefix + "width", size[0], false);
         this.global.set(prefix + "height", size[1], false);
         this.global.set(prefix + "x", position[0], false);
         this.global.set(prefix + "y", position[1], true);
      }
   }

   public Console.CloseAction getCloseAction() {
      return this.close;
   }

   public void setCloseAction(Console.CloseAction action) {
      this.close = action;
   }

   private void onClose() {
      if (this.close != null) {
         switch($SWITCH_TABLE$com$turikhay$tlauncher$util$Console$CloseAction()[this.close.ordinal()]) {
         case 1:
            this.kill();
         case 2:
            TLauncher.kill();
         default:
         }
      }
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

   public static void updateLocale() {
      Iterator var1 = frames.iterator();

      while(var1.hasNext()) {
         ConsoleFrame frame = (ConsoleFrame)var1.next();
         frame.updateLocale();
      }

   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$util$Console$CloseAction() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$util$Console$CloseAction;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Console.CloseAction.values().length];

         try {
            var0[Console.CloseAction.EXIT.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Console.CloseAction.KILL.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$util$Console$CloseAction = var0;
         return var0;
      }
   }

   public static enum CloseAction {
      KILL,
      EXIT;
   }
}
