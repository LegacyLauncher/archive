package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.U;
import com.turikhay.util.logger.LinkedOutputStream;
import com.turikhay.util.logger.Logger;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Console extends PrintStream implements Logger {
   private static List frames = Collections.synchronizedList(new ArrayList());
   private final GlobalSettings global;
   private final LinkedOutputStream stream;
   private final ConsoleFrame cf;
   private String del;
   private boolean killed;
   private Console.CloseAction close;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$Console$CloseAction;

   public Console(GlobalSettings global, LinkedOutputStream stream, String name, boolean show) {
      super(new BufferedOutputStream(stream), true);
      this.del = null;
      this.global = global;
      this.stream = stream;
      this.stream.setLogger(this);
      this.cf = new ConsoleFrame(this, global, name);
      this.init();
      if (show) {
         this.cf.setVisible(true);
      }

      frames.add(this.cf);
      this.log(stream.getOutput());
   }

   public Console(GlobalSettings global, String name, boolean show) {
      this(global, new LinkedOutputStream(), name, show);
   }

   public Console(LinkedOutputStream stream, String name, boolean show) {
      this((GlobalSettings)null, stream, name, show);
   }

   private void init() {
      this.cf.addWindowListener(new WindowListener() {
         public void windowOpened(WindowEvent e) {
         }

         public void windowClosing(WindowEvent e) {
            Console.this.save();
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
      this.cf.addComponentListener(new ComponentListener() {
         public void componentResized(ComponentEvent e) {
            Console.this.save(false);
         }

         public void componentMoved(ComponentEvent e) {
            Console.this.save(false);
         }

         public void componentShown(ComponentEvent e) {
            Console.this.save(true);
         }

         public void componentHidden(ComponentEvent e) {
            Console.this.save(true);
         }
      });
      frames.add(this.cf);
      if (this.global != null) {
         String prefix = "gui.console.";
         int width = this.global.getInteger(prefix + "width", 620);
         int height = this.global.getInteger(prefix + "height", 400);
         int x = this.global.getInteger(prefix + "x", 0);
         int y = this.global.getInteger(prefix + "y", 0);
         prefix = prefix + "search.";
         boolean mcase = this.global.getBoolean(prefix + "mcase");
         boolean whole = this.global.getBoolean(prefix + "whole");
         boolean cycle = this.global.getBoolean(prefix + "cycle");
         boolean regexp = this.global.getBoolean(prefix + "regexp");
         this.cf.setSize(width, height);
         this.cf.setLocation(x, y);
         SearchPrefs sf = this.cf.getSearchPrefs();
         sf.setCaseSensetive(mcase);
         sf.setWordSearch(whole);
         sf.setCycled(cycle);
         sf.setRegExp(regexp);
      }
   }

   public void setShown(boolean shown) {
      if (shown) {
         this.show();
      } else {
         this.hide();
      }

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

   public void clear() {
      this.check();
      this.stream.clear();
      this.cf.clear();
   }

   public void kill() {
      this.check();
      this.save();
      this.cf.setVisible(false);
      this.cf.clear();
      frames.remove(this.cf);
      this.killed = true;
   }

   public void killIn(long millis) {
      this.check();
      this.save();
      this.cf.hideIn(millis);
      AsyncThread.execute(new Runnable() {
         public void run() {
            if (!Console.this.cf.isVisible()) {
               Console.this.kill();
            }

         }
      }, millis + 1000L);
   }

   public boolean isKilled() {
      this.check();
      return this.killed;
   }

   public boolean isHidden() {
      this.check();
      return !this.cf.isShowing();
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
      return this.stream.getOutput();
   }

   public ConsoleFrame getFrame() {
      return this.cf;
   }

   public void save() {
      this.save(true);
   }

   public void save(boolean flush) {
      this.check();
      if (this.global != null) {
         String prefix = "gui.console.";
         int[] size = this.getSize();
         int[] position = this.getPosition();
         this.global.set(prefix + "width", size[0], false);
         this.global.set(prefix + "height", size[1], false);
         this.global.set(prefix + "x", position[0], false);
         this.global.set(prefix + "y", position[1], false);
         prefix = prefix + "search.";
         boolean[] prefs = this.cf.getSearchPrefs().get();
         this.global.set(prefix + "mcase", prefs[0], false);
         this.global.set(prefix + "whole", prefs[1], false);
         this.global.set(prefix + "cycle", prefs[2], false);
         this.global.set(prefix + "regexp", prefs[3], flush);
      }
   }

   public void rawlog(int c) {
      this.check();
      this.cf.print((char)c);
   }

   public void log(Object... obj) {
      this.check();
      this.cf.print(this.del);
      this.cf.print(U.toLog(obj));
   }

   public Console.CloseAction getCloseAction() {
      return this.close;
   }

   public void setCloseAction(Console.CloseAction action) {
      this.close = action;
   }

   private void onClose() {
      if (this.close != null) {
         switch($SWITCH_TABLE$com$turikhay$tlauncher$ui$Console$CloseAction()[this.close.ordinal()]) {
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
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$Console$CloseAction() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$ui$Console$CloseAction;
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

         $SWITCH_TABLE$com$turikhay$tlauncher$ui$Console$CloseAction = var0;
         return var0;
      }
   }

   public static enum CloseAction {
      KILL,
      EXIT;
   }
}
