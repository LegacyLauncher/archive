package ru.turikhay.tlauncher.ui.progress;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.LineMetrics;
import javax.swing.JProgressBar;
import ru.turikhay.util.U;

public class ProgressBar extends JProgressBar {
   public static int DEFAULT_HEIGHT = 20;
   private static int BOUNDS_SIZE = 3;
   private static int BORDER_SIZE = 10;
   private static int EDGE_CHARS = 50;
   private static int CENTER_CHARS = 30;
   private static float DEFAULT_FONT_SIZE = 12.0F;
   private static final long serialVersionUID = -8095192709934629794L;
   private final Object sync;
   private final Component parent;
   private String wS;
   private String cS;
   private String eS;
   private boolean wS_changed;
   private boolean cS_changed;
   private boolean eS_changed;
   private final int[] wS_bounds;
   private final int[] cS_bounds;
   private final int[] eS_bounds;
   private int oldWidth;

   public ProgressBar(Component parentComp) {
      this.sync = new Object();
      this.wS_bounds = new int[BOUNDS_SIZE];
      this.cS_bounds = new int[BOUNDS_SIZE];
      this.eS_bounds = new int[BOUNDS_SIZE];
      this.parent = parentComp;
      if (this.parent != null) {
         this.parent.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
               ProgressBar.this.updateSize();
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
         });
      }

      this.setFont(this.getFont().deriveFont(DEFAULT_FONT_SIZE));
      this.setOpaque(false);
   }

   public ProgressBar() {
      this((Component)null);
   }

   private void updateSize() {
      if (this.parent != null) {
         this.setPreferredSize(new Dimension(this.parent.getWidth(), DEFAULT_HEIGHT));
      }
   }

   public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint) {
      if (acceptNull || west != null) {
         this.setWestString(west, false);
      }

      if (acceptNull || center != null) {
         this.setCenterString(center, false);
      }

      if (acceptNull || east != null) {
         this.setEastString(east, false);
      }

      if (repaint) {
         this.repaint();
      }

   }

   public void setStrings(String west, String center, String east) {
      this.setStrings(west, center, east, true, true);
   }

   public void setWestString(String string, boolean update) {
      string = U.r(string, EDGE_CHARS);
      this.wS_changed = this.wS != string;
      this.wS = string;
      if (this.wS_changed && update) {
         this.repaint();
      }

   }

   public void setWestString(String string) {
      this.setWestString(string, true);
   }

   public void setCenterString(String string, boolean update) {
      string = U.r(string, CENTER_CHARS);
      this.cS_changed = this.cS != string;
      this.cS = string;
      if (this.cS_changed && update) {
         this.repaint();
      }

   }

   public void setCenterString(String string) {
      this.setCenterString(string, true);
   }

   public void setEastString(String string, boolean update) {
      string = U.r(string, EDGE_CHARS);
      this.eS_changed = this.eS != string;
      this.eS = string;
      if (this.eS_changed && update) {
         this.repaint();
      }

   }

   public void setEastString(String string) {
      this.setEastString(string, true);
   }

   public void clearProgress() {
      this.setIndeterminate(false);
      this.setValue(0);
      this.setStrings((String)null, (String)null, (String)null, true, false);
   }

   public void startProgress() {
      this.clearProgress();
      this.updateSize();
      this.setVisible(true);
   }

   public void stopProgress() {
      this.setVisible(false);
      this.clearProgress();
   }

   private void draw(Graphics g) {
      boolean drawWest = this.wS != null;
      boolean drawCenter = this.cS != null;
      boolean drawEast = this.eS != null;
      if (drawWest || drawCenter || drawEast) {
         Font font = g.getFont();
         FontMetrics fm = g.getFontMetrics(font);
         int width = this.getWidth();
         boolean force = width != this.oldWidth;
         this.oldWidth = width;
         LineMetrics lm;
         if (drawCenter && (force || this.cS_changed)) {
            lm = fm.getLineMetrics(this.cS, g);
            this.cS_bounds[1] = fm.stringWidth(this.cS);
            this.cS_bounds[2] = (int)lm.getHeight();
            this.cS_bounds[0] = width / 2 - this.cS_bounds[1] / 2;
            this.cS_changed = false;
         }

         if (drawWest && (force || this.wS_changed)) {
            lm = fm.getLineMetrics(this.wS, g);
            this.wS_bounds[1] = fm.stringWidth(this.wS);
            this.wS_bounds[2] = (int)lm.getHeight();
            this.wS_bounds[0] = BORDER_SIZE;
            this.wS_changed = false;
         }

         if (drawEast && (force || this.eS_changed)) {
            lm = fm.getLineMetrics(this.eS, g);
            this.eS_bounds[1] = fm.stringWidth(this.eS);
            this.eS_bounds[2] = (int)lm.getHeight();
            this.eS_bounds[0] = width - this.eS_bounds[1] - BORDER_SIZE;
            this.eS_changed = false;
         }

         Graphics2D g2D = (Graphics2D)g;
         g.setColor(Color.black);
         g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g.setFont(font);
         drawString(g, this.wS, this.wS_bounds);
         drawString(g, this.cS, this.cS_bounds);
         drawString(g, this.eS, this.eS_bounds);
      }
   }

   private static void drawString(Graphics g, String s, int[] bounds) {
      if (s != null) {
         g.setColor(Color.white);

         for(int x = -1; x < 2; ++x) {
            for(int y = -1; y < 2; ++y) {
               g.drawString(s, bounds[0] + x, bounds[2] + y);
            }
         }

         g.setColor(Color.black);
         g.drawString(s, bounds[0], bounds[2]);
      }
   }

   public void update(Graphics g) {
      try {
         super.update(g);
      } catch (Exception var4) {
         U.log("Error updating progress bar:", var4.toString());
         return;
      }

      synchronized(this.sync) {
         this.draw(g);
      }
   }

   public void paint(Graphics g) {
      try {
         super.paint(g);
      } catch (Exception var4) {
         U.log("Error paining progress bar:", var4.toString());
         return;
      }

      synchronized(this.sync) {
         this.draw(g);
      }
   }
}
