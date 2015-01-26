package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;

public class ExtendedPanel extends JPanel {
   private final List mouseListeners;
   private Insets insets;
   private float opacity;
   private AlphaComposite aComp;

   public ExtendedPanel(LayoutManager layout, boolean isDoubleBuffered) {
      super(layout, isDoubleBuffered);
      this.opacity = 1.0F;
      this.mouseListeners = new ArrayList();
      this.setOpaque(false);
   }

   public ExtendedPanel(LayoutManager layout) {
      this(layout, true);
   }

   public ExtendedPanel(boolean isDoubleBuffered) {
      this(new FlowLayout(), isDoubleBuffered);
   }

   public ExtendedPanel() {
      this(true);
   }

   public float getOpacity() {
      return this.opacity;
   }

   public void setOpacity(float f) {
      if (!(f < 0.0F) && !(f > 1.0F)) {
         this.opacity = f;
         this.aComp = AlphaComposite.getInstance(3, f);
         this.repaint();
      } else {
         throw new IllegalArgumentException("opacity must be in [0;1]");
      }
   }

   public Insets getInsets() {
      return this.insets == null ? super.getInsets() : this.insets;
   }

   public void setInsets(Insets insets) {
      this.insets = insets;
   }

   public Component add(Component comp) {
      super.add(comp);
      if (comp == null) {
         return null;
      } else {
         MouseListener[] compareListeners = comp.getMouseListeners();
         Iterator var4 = this.mouseListeners.iterator();

         while(var4.hasNext()) {
            MouseListener listener = (MouseListener)var4.next();
            MouseListener add = listener;
            MouseListener[] var9 = compareListeners;
            int var8 = compareListeners.length;

            for(int var7 = 0; var7 < var8; ++var7) {
               MouseListener compareListener = var9[var7];
               if (listener.equals(compareListener)) {
                  add = null;
                  break;
               }
            }

            if (add != null) {
               comp.addMouseListener(add);
            }
         }

         return comp;
      }
   }

   public void add(Component... components) {
      if (components == null) {
         throw new NullPointerException();
      } else {
         Component[] var5 = components;
         int var4 = components.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component comp = var5[var3];
            this.add(comp);
         }

      }
   }

   public void add(Component component0, Component component1) {
      this.add(component0, component1);
   }

   public synchronized void addMouseListener(MouseListener listener) {
      if (listener != null) {
         this.mouseListeners.add(listener);
         Component[] var5;
         int var4 = (var5 = this.getComponents()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component comp = var5[var3];
            comp.addMouseListener(listener);
         }

      }
   }

   protected synchronized void addMouseListenerOriginally(MouseListener listener) {
      super.addMouseListener(listener);
   }

   public synchronized void removeMouseListener(MouseListener listener) {
      if (listener != null) {
         this.mouseListeners.remove(listener);
         Component[] var5;
         int var4 = (var5 = this.getComponents()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component comp = var5[var3];
            comp.removeMouseListener(listener);
         }

      }
   }

   protected synchronized void removeMouseListenerOriginally(MouseListener listener) {
      super.removeMouseListener(listener);
   }

   public boolean contains(Component comp) {
      if (comp == null) {
         return false;
      } else {
         Component[] var5;
         int var4 = (var5 = this.getComponents()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component c = var5[var3];
            if (comp.equals(c)) {
               return true;
            }
         }

         return false;
      }
   }

   public Insets setInsets(int top, int left, int bottom, int right) {
      Insets insets = new Insets(top, left, bottom, right);
      this.setInsets(insets);
      return insets;
   }

   protected void paintComponent(Graphics g0) {
      if (this.opacity == 1.0F) {
         super.paintComponent(g0);
      } else {
         Graphics2D g = (Graphics2D)g0;
         g.setComposite(this.aComp);
         super.paintComponent(g0);
      }
   }
}
