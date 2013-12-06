package com.turikhay.tlauncher.ui;

import com.turikhay.util.U;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.LineMetrics;
import javax.swing.JProgressBar;

public class ProgressBar extends JProgressBar implements LocalizableComponent {
   private static final long serialVersionUID = 5163801043869691008L;
   private Object sync = new Object();
   private String center_string;
   private String west_string;
   private String east_string;
   private boolean started = false;
   private boolean wait = false;
   private boolean center_string_changed = true;
   private boolean west_string_changed = true;
   private boolean east_string_changed = true;
   private int center_tw;
   private int center_th;
   private int center_x;
   private int center_y;
   private int west_th;
   private int west_x;
   private int west_y;
   private int east_tw;
   private int east_th;
   private int east_x;
   private int east_y;
   private int oldw;
   private int border = 10;

   ProgressBar(TLauncherFrame f) {
      this.setMinimum(0);
      this.setMaximum(100);
      this.setPreferredSize(new Dimension(f.getWidth(), 20));
      this.paintBorder = false;
      this.setVisible(false);
   }

   public void progressStart() {
      while(this.wait) {
         this.sleepFor(10L);
      }

      this.setVisible(true);
      this.setValue(0);
      this.started = true;
   }

   public void progressStop() {
      while(this.wait) {
         this.sleepFor(10L);
      }

      this.setWestString((String)null);
      this.setCenterString((String)null);
      this.setEastString((String)null);
      this.setVisible(false);
      this.started = false;
   }

   public boolean isStarted() {
      return this.started;
   }

   public void setCenterString(String str) {
      str = U.r(str, 70);

      while(this.wait) {
         this.sleepFor(10L);
      }

      this.center_string_changed = this.center_string != str;
      this.center_string = str;
      this.repaint();
   }

   public String getCenterString() {
      return this.center_string;
   }

   public void setWestString(String str) {
      str = U.r(str, 50);

      while(this.wait) {
         this.sleepFor(10L);
      }

      this.west_string_changed = this.west_string != str;
      this.west_string = str;
      this.repaint();
   }

   public String getWestString() {
      return this.west_string;
   }

   public void setEastString(String str) {
      str = U.r(str, 50);

      while(this.wait) {
         this.sleepFor(10L);
      }

      this.east_string_changed = this.east_string != str;
      this.east_string = str;
      this.repaint();
   }

   public String getEastString() {
      return this.east_string;
   }

   public void update(Graphics g) {
      synchronized(this.sync) {
         super.update(g);
      }

      this.paint(g);
   }

   public void paint(Graphics g) {
      synchronized(this.sync) {
         this.paint_(g);
      }
   }

   private void paint_(Graphics g) {
      super.paint(g);
      boolean center = this.center_string != null;
      boolean west = this.west_string != null;
      boolean east = this.east_string != null;
      boolean force = false;
      if (center || west || east) {
         this.wait = true;
         FontMetrics fm = g.getFontMetrics();
         int w = this.getWidth();
         if (this.oldw != w) {
            force = true;
         }

         LineMetrics lm;
         if (center && (force || this.center_string_changed)) {
            lm = fm.getLineMetrics(this.center_string, g);
            this.center_tw = fm.stringWidth(this.center_string);
            this.center_th = (int)lm.getHeight();
            this.center_x = w / 2 - this.center_tw / 2;
            this.center_y = this.center_th;
            this.center_string_changed = false;
         }

         if (west && (force || this.west_string_changed)) {
            lm = fm.getLineMetrics(this.west_string, g);
            this.west_th = (int)lm.getHeight();
            this.west_x = this.border;
            this.west_y = this.west_th;
            this.west_string_changed = false;
         }

         if (east && (force || this.east_string_changed)) {
            lm = fm.getLineMetrics(this.east_string, g);
            this.east_tw = fm.stringWidth(this.east_string);
            this.east_th = (int)lm.getHeight();
            this.east_x = w - this.east_tw - this.border;
            this.east_y = this.east_th;
            this.east_string_changed = false;
         }

         Color oldcolor = g.getColor();
         g.setColor(Color.black);
         Graphics2D g2D = (Graphics2D)g;
         g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         if (center) {
            g2D.drawString(this.center_string, this.center_x, this.center_y);
         }

         if (west) {
            g2D.drawString(this.west_string, this.west_x, this.west_y);
         }

         if (east) {
            g2D.drawString(this.east_string, this.east_x, this.east_y);
         }

         this.oldw = w;
         g.setColor(oldcolor);
         this.wait = false;
      }
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }

   public void updateLocale() {
      this.repaint();
   }
}
