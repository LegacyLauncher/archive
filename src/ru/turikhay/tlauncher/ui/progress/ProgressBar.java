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
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class ProgressBar extends JProgressBar {
   private final Color textColor;
   public static int DEFAULT_HEIGHT = SwingUtil.magnify(20);
   private static int BORDER_SIZE = SwingUtil.magnify(10);
   private static int EDGE_CHARS = 50;
   private static int CENTER_CHARS = 30;
   private final Object sync;
   private final Component parent;
   private String wS;
   private String cS;
   private String eS;
   private boolean wS_changed;
   private boolean cS_changed;
   private boolean eS_changed;
   private int wS_x;
   private int cS_x;
   private int eS_x;
   private int oldWidth;

   public ProgressBar(Component parentComp) {
      this.textColor = (new JLabel()).getForeground();
      this.sync = new Object();
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

      this.setFont(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
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
      string = StringUtil.cut(string, EDGE_CHARS);
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
      string = StringUtil.cut(string, CENTER_CHARS);
      this.cS_changed = string == null ? this.cS == null : !string.equals(this.cS);
      this.cS = string;
      if (this.cS_changed && update) {
         this.repaint();
      }

   }

   public void setCenterString(String string) {
      this.setCenterString(string, true);
   }

   public void setEastString(String string, boolean update) {
      string = StringUtil.cut(string, EDGE_CHARS);
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
         if (drawCenter && (force || this.cS_changed)) {
            this.cS_x = width / 2 - fm.stringWidth(this.cS) / 2;
            this.cS_changed = false;
         }

         if (drawWest && (force || this.wS_changed)) {
            this.wS_x = BORDER_SIZE;
            this.wS_changed = false;
         }

         if (drawEast && (force || this.eS_changed)) {
            this.eS_x = width - fm.stringWidth(this.eS) - BORDER_SIZE;
            this.eS_changed = false;
         }

         Graphics2D g2D = (Graphics2D)g;
         g.setColor(this.textColor);
         g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g.setFont(font);
         this.drawString(g, this.wS, this.wS_x);
         this.drawString(g, this.cS, this.cS_x);
         this.drawString(g, this.eS, this.eS_x);
      }

   }

   private void drawString(Graphics g, String s, int x) {
      if (s != null) {
         int y = (this.getHeight() - g.getFontMetrics().getDescent() + g.getFontMetrics().getAscent()) / 2;
         g.setColor(Color.white);

         for(int borderX = -1; borderX < 2; ++borderX) {
            for(int borderY = -1; borderY < 2; ++borderY) {
               g.drawString(s, x + borderX, y + borderY);
            }
         }

         g.setColor(Color.black);
         g.drawString(s, x, y);
      }

   }

   public void update(Graphics g) {
      try {
         super.update(g);
      } catch (Exception var6) {
         U.log("Error updating progress bar:", var6.toString());
         return;
      }

      Object e = this.sync;
      synchronized(this.sync) {
         this.draw(g);
      }
   }

   public void paint(Graphics g) {
      try {
         super.paint(g);
      } catch (Exception var6) {
         U.log("Error paining progress bar:", var6.toString());
         return;
      }

      Object e = this.sync;
      synchronized(this.sync) {
         this.draw(g);
      }
   }
}
