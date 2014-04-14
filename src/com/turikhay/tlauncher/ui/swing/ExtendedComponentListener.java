package com.turikhay.tlauncher.ui.swing;

import com.turikhay.tlauncher.ui.swing.util.IntegerArrayGetter;
import com.turikhay.util.U;
import com.turikhay.util.async.LoopedThread;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public abstract class ExtendedComponentListener implements ComponentListener {
   private final Component comp;
   private final ExtendedComponentListener.QuickParameterListenerThread resizeListener;
   private final ExtendedComponentListener.QuickParameterListenerThread moveListener;
   private ComponentEvent lastResizeEvent;
   private ComponentEvent lastMoveEvent;

   public ExtendedComponentListener(Component component) {
      if (component == null) {
         throw new NullPointerException();
      } else {
         this.comp = component;
         this.resizeListener = new ExtendedComponentListener.QuickParameterListenerThread(new IntegerArrayGetter() {
            public int[] getIntegerArray() {
               return new int[]{ExtendedComponentListener.this.comp.getWidth(), ExtendedComponentListener.this.comp.getHeight()};
            }
         }, new Runnable() {
            public void run() {
               ExtendedComponentListener.this.onComponentResized(ExtendedComponentListener.this.lastResizeEvent);
            }
         });
         this.moveListener = new ExtendedComponentListener.QuickParameterListenerThread(new IntegerArrayGetter() {
            public int[] getIntegerArray() {
               Point location = ExtendedComponentListener.this.comp.getLocation();
               return new int[]{location.x, location.y};
            }
         }, new Runnable() {
            public void run() {
               ExtendedComponentListener.this.onComponentMoved(ExtendedComponentListener.this.lastMoveEvent);
            }
         });
      }
   }

   public final void componentResized(ComponentEvent e) {
      this.onComponentResizing(e);
      this.resizeListener.startListening();
   }

   public final void componentMoved(ComponentEvent e) {
      this.onComponentMoving(e);
      this.moveListener.startListening();
   }

   public abstract void onComponentResizing(ComponentEvent var1);

   public abstract void onComponentResized(ComponentEvent var1);

   public abstract void onComponentMoving(ComponentEvent var1);

   public abstract void onComponentMoved(ComponentEvent var1);

   private class QuickParameterListenerThread extends LoopedThread {
      private static final int TICK = 500;
      private final IntegerArrayGetter paramGetter;
      private final Runnable runnable;

      QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run) {
         super("QuickParameterListenerThread");
         this.paramGetter = getter;
         this.runnable = run;
         this.startAndWait();
      }

      void startListening() {
         this.iterate();
      }

      protected void iterateOnce() {
         int[] initial = this.paramGetter.getIntegerArray();
         boolean var3 = false;

         boolean equal;
         do {
            this.sleep();
            int[] newvalue = this.paramGetter.getIntegerArray();
            equal = true;

            for(int i = 0; i < initial.length; ++i) {
               if (initial[i] != newvalue[i]) {
                  equal = false;
               }
            }

            initial = newvalue;
         } while(!equal);

         this.runnable.run();
      }

      private void sleep() {
         U.sleepFor(500L);
      }
   }
}
