package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import ru.turikhay.tlauncher.ui.swing.util.IntegerArrayGetter;

public abstract class ExtendedComponentListener implements ComponentListener {
   private final Component comp;
   private final QuickParameterListenerThread resizeListener;
   private final QuickParameterListenerThread moveListener;
   private ComponentEvent lastResizeEvent;
   private ComponentEvent lastMoveEvent;

   public ExtendedComponentListener(Component component, int tick) {
      if (component == null) {
         throw new NullPointerException();
      } else {
         this.comp = component;
         this.resizeListener = new QuickParameterListenerThread(new IntegerArrayGetter() {
            public int[] getIntegerArray() {
               return new int[]{ExtendedComponentListener.this.comp.getWidth(), ExtendedComponentListener.this.comp.getHeight()};
            }
         }, new Runnable() {
            public void run() {
               ExtendedComponentListener.this.onComponentResized(ExtendedComponentListener.this.lastResizeEvent);
            }
         }, tick);
         this.moveListener = new QuickParameterListenerThread(new IntegerArrayGetter() {
            public int[] getIntegerArray() {
               Point location = ExtendedComponentListener.this.comp.getLocation();
               return new int[]{location.x, location.y};
            }
         }, new Runnable() {
            public void run() {
               ExtendedComponentListener.this.onComponentMoved(ExtendedComponentListener.this.lastMoveEvent);
            }
         }, tick);
      }
   }

   public ExtendedComponentListener(Component component) {
      this(component, 500);
   }

   public final void componentResized(ComponentEvent e) {
      this.onComponentResizing(e);
      this.resizeListener.startListening();
   }

   public final void componentMoved(ComponentEvent e) {
      this.onComponentMoving(e);
      this.moveListener.startListening();
   }

   public boolean isListening() {
      return this.resizeListener.isIterating() || this.moveListener.isIterating();
   }

   public abstract void onComponentResizing(ComponentEvent var1);

   public abstract void onComponentResized(ComponentEvent var1);

   public abstract void onComponentMoving(ComponentEvent var1);

   public abstract void onComponentMoved(ComponentEvent var1);
}
