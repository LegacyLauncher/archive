package ru.turikhay.tlauncher.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class Dragger extends BorderPanel implements LocalizableComponent {
   private static final List draggers = new ArrayList();
   private static final Color enabledColor = new Color(0, 0, 0, 32);
   private static final Color disabledColor = new Color(0, 0, 0, 16);
   private static Configuration config;
   private static Point maxPoint;
   private static boolean ready;
   private final JComponent parent;
   private final String key;
   private final ExtendedLabel label;
   private final Dragger.DraggerMouseListener listener;
   private String tooltip;

   public Dragger(JComponent parent, String name) {
      if (parent == null) {
         throw new NullPointerException("parent");
      } else if (name == null) {
         throw new NullPointerException("name");
      } else if (StringUtils.isEmpty(name)) {
         throw new IllegalArgumentException("name is empty");
      } else {
         this.parent = parent;
         this.key = "dragger." + name;
         this.listener = new Dragger.DraggerMouseListener();
         this.setCursor(SwingUtil.getCursor(13));
         this.label = new ExtendedLabel();
         this.label.addMouseListener(this.listener);
         this.setCenter(this.label);
         if (!ready) {
            draggers.add(new SoftReference(this));
         }

         this.setEnabled(true);
      }
   }

   public void paint(Graphics g0) {
      Graphics2D g = (Graphics2D)g0;
      g.setColor(this.isEnabled() ? enabledColor : disabledColor);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());
      super.paintComponent(g);
   }

   public void setEnabled(boolean b) {
      this.tooltip = b ? "dragger.label" : null;
      this.updateLocale();
      super.setEnabled(b);
   }

   private void dragComponent(int x, int y) {
      if (ready) {
         if (x + this.parent.getWidth() > maxPoint.x) {
            x = maxPoint.x - this.parent.getWidth();
         }

         if (x < 0) {
            x = 0;
         }

         if (y + this.parent.getHeight() > maxPoint.y) {
            y = maxPoint.y - this.parent.getHeight();
         }

         if (y < 0) {
            y = 0;
         }

         this.parent.setLocation(x, y);
         config.set(this.key, new IntegerArray(new int[]{x, y}));
         U.log(x, y);
      }

   }

   public void updateCoords() {
      this.dragComponent(this.parent.getX(), this.parent.getY());
   }

   public void loadCoords() {
      IntegerArray arr;
      try {
         arr = IntegerArray.parseIntegerArray(config.get(this.key));
         if (arr.size() != 2) {
            throw new IllegalArgumentException("illegal size");
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         return;
      }

      this.dragComponent(arr.get(0), arr.get(1));
   }

   private void ready() {
      this.updateLocale();
      this.loadCoords();
   }

   public void updateLocale() {
      this.label.setToolTipText(Localizable.get(this.tooltip));
   }

   public static synchronized void ready(Configuration config, Point maxPoint) {
      if (!ready) {
         if (config == null) {
            throw new NullPointerException("config");
         }

         if (maxPoint == null) {
            throw new NullPointerException("maxPoint");
         }

         ready = true;
         Iterator var3 = draggers.iterator();

         while(var3.hasNext()) {
            SoftReference dragger = (SoftReference)var3.next();
            if (dragger.get() == null) {
               U.log("dragger has been deleted :(");
            } else {
               ((Dragger)dragger.get()).ready();
            }
         }
      }

   }

   public static synchronized void update() {
      Iterator var1 = draggers.iterator();

      while(var1.hasNext()) {
         SoftReference dragger = (SoftReference)var1.next();
         if (dragger.get() == null) {
            U.log("dragger has been deleted :(");
         } else {
            ((Dragger)dragger.get()).updateCoords();
         }
      }

   }

   public class DraggerMouseListener extends MouseAdapter {
      private int[] startPoint = new int[2];

      public void mousePressed(MouseEvent e) {
         if (Dragger.this.isEnabled()) {
            this.startPoint[0] = e.getX();
            this.startPoint[1] = e.getY();
         }

      }

      public void mouseReleased(MouseEvent e) {
         if (Dragger.this.isEnabled()) {
            Dragger.this.dragComponent(Dragger.this.parent.getX() + e.getX() - this.startPoint[0], Dragger.this.parent.getY() + e.getY() - this.startPoint[1]);
         }

      }
   }
}
