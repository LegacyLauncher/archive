package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.util.U;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.font.LineMetrics;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class ProgressBar extends JProgressBar implements LocalizableComponent {
   private static int DEFAULT_HEIGHT = 20;
   private static int BOUNDS_SIZE = 3;
   private static int BORDER_SIZE = 10;
   private static int EDGE_CHARS = 50;
   private static int CENTER_CHARS = 20;
   private static final long serialVersionUID = 4988683059704813021L;
   private final ProgressBar instance;
   private final Object sync;
   private final JFrame parent;
   private Settings lang;
   private DownloadListener listener;
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

   public ProgressBar(JFrame frame, Downloader downloader, Settings language) {
      this.instance = this;
      this.sync = new Object();
      this.wS_bounds = new int[BOUNDS_SIZE];
      this.cS_bounds = new int[BOUNDS_SIZE];
      this.eS_bounds = new int[BOUNDS_SIZE];
      this.parent = frame;
      this.parent.addComponentListener(new ComponentListener() {
         public void componentResized(ComponentEvent e) {
            ProgressBar.this.instance.setPreferredSize(new Dimension(ProgressBar.this.parent.getWidth(), ProgressBar.DEFAULT_HEIGHT));
         }

         public void componentMoved(ComponentEvent e) {
         }

         public void componentShown(ComponentEvent e) {
         }

         public void componentHidden(ComponentEvent e) {
         }
      });
      if (language != null) {
         this.lang = language;
      }

      if (downloader != null) {
         this.listener = new DownloadListener() {
            public void onDownloaderStart(Downloader d, int files) {
               ProgressBar.this.instance.startProgress();
               ProgressBar.this.setIndeterminate(true);
               ProgressBar.this.setCenterString(ProgressBar.this.lang.get("progressBar.init"));
               ProgressBar.this.setEastString(ProgressBar.this.lang.get("progressBar.downloading" + (files == 1 ? "-one" : ""), "0", files));
            }

            public void onDownloaderAbort(Downloader d) {
               ProgressBar.this.instance.stopProgress();
            }

            public void onDownloaderComplete(Downloader d) {
               ProgressBar.this.instance.stopProgress();
            }

            public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {
            }

            public void onDownloaderProgress(Downloader d, int progress, double speed) {
               if (progress > 0) {
                  if (ProgressBar.this.getValue() > progress) {
                     return;
                  }

                  ProgressBar.this.setIndeterminate(false);
                  ProgressBar.this.setValue(progress);
                  ProgressBar.this.setCenterString(progress + "%");
               }

            }

            public void onDownloaderFileComplete(Downloader d, Downloadable file) {
               ProgressBar.this.setIndeterminate(false);
               ProgressBar.this.setWestString(ProgressBar.this.lang.get("progressBar.completed", "0", file.getFilename()));
               ProgressBar.this.setEastString(ProgressBar.this.lang.get("progressBar.remaining", "0", d.getRemaining()));
            }
         };
         downloader.addListener(this.listener);
      }

      this.setPreferredSize(new Dimension(this.parent.getWidth(), DEFAULT_HEIGHT));
      this.stopProgress();
   }

   public ProgressBar(TLauncherFrame f) {
      this(f, f.tlauncher.getDownloader(), f.lang);
   }

   public void setStrings(String west, String center, String east, boolean acceptNull) {
      if (acceptNull || west != null) {
         this.setWestString(west, false);
      }

      if (acceptNull || center != null) {
         this.setCenterString(center, false);
      }

      if (acceptNull || east != null) {
         this.setEastString(east, false);
      }

      this.repaint();
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
      synchronized(this.sync) {
         this.setIndeterminate(false);
         this.setValue(0);
         this.setWestString((String)null, false);
         this.setCenterString((String)null, false);
         this.setEastString((String)null, false);
      }
   }

   public void startProgress() {
      synchronized(this.sync) {
         this.clearProgress();
         this.setVisible(true);
      }
   }

   public void stopProgress() {
      synchronized(this.sync) {
         this.setVisible(false);
         this.clearProgress();
      }
   }

   public void update(Graphics g) {
      synchronized(this.sync) {
         super.update(g);
      }

      this.paint(g);
   }

   public void paint(Graphics g) {
      synchronized(this.sync) {
         this.draw(g);
      }
   }

   private void draw(Graphics g) {
      try {
         super.paint(g);
      } catch (Exception var9) {
         U.log("Error paining progress bar:", var9.toString());
         return;
      }

      boolean drawWest = this.wS != null;
      boolean drawCenter = this.cS != null;
      boolean drawEast = this.eS != null;
      if (drawWest || drawCenter || drawEast) {
         FontMetrics fm = g.getFontMetrics();
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
         g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         this.drawString(g, this.wS, this.wS_bounds);
         this.drawString(g, this.cS, this.cS_bounds);
         this.drawString(g, this.eS, this.eS_bounds);
      }
   }

   private void drawString(Graphics g, String s, int[] bounds) {
      if (s != null) {
         g.drawString(s, bounds[0], bounds[2]);
      }
   }

   public void updateLocale() {
      this.repaint();
   }
}
