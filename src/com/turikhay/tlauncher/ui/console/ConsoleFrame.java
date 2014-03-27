package com.turikhay.tlauncher.ui.console;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.swing.TextPopup;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

public class ConsoleFrame extends JFrame implements LocalizableComponent {
   private static final long serialVersionUID = 5667131709333334581L;
   public static final int minWidth = 670;
   public static final int minHeight = 500;
   private int w = 670;
   private int h = 500;
   private int v = 0;
   boolean update = true;
   private final Object busy = new Object();
   private final JPanel panel;
   private final SearchPanel sp;
   private final ExitCancelPanel ecp;
   private final TextPopup textpopup;
   final JTextArea textArea;
   private final JScrollBar scrollBar;
   private final BoundedRangeModel scrollBarModel;
   final Console c;
   private boolean hiding = false;

   public ConsoleFrame(Console c, Configuration s, String name) {
      super(name);
      if (c == null) {
         throw new NullPointerException("Console can't be NULL!");
      } else {
         this.c = c;
         LayoutManager layout = new BorderLayout();
         this.panel = new JPanel(layout);
         this.panel.setAlignmentX(0.5F);
         this.panel.setAlignmentY(0.5F);
         Font font = new Font("DialogInput", 0, 14);
         this.textpopup = new TextPopup();
         this.textArea = new JTextArea();
         this.textArea.setLineWrap(true);
         this.textArea.setEditable(false);
         this.textArea.setMargin(new Insets(0, 0, 0, 0));
         this.textArea.setFont(font);
         this.textArea.setForeground(Color.white);
         this.textArea.setCaretColor(Color.white);
         this.textArea.setBackground(Color.black);
         this.textArea.setSelectionColor(Color.gray);
         this.textArea.setAutoscrolls(true);
         ((DefaultCaret)this.textArea.getCaret()).setUpdatePolicy(2);
         this.textArea.addMouseListener(this.textpopup);
         JScrollPane scrollPane = new JScrollPane(this.textArea);
         scrollPane.setBorder((Border)null);
         scrollPane.setVerticalScrollBarPolicy(20);
         this.scrollBar = scrollPane.getVerticalScrollBar();
         this.scrollBarModel = this.scrollBar.getModel();
         this.scrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
               boolean resizing = ConsoleFrame.this.getWidth() != ConsoleFrame.this.w;
               if (!resizing) {
                  int nv = e.getValue();
                  if (nv < ConsoleFrame.this.v) {
                     ConsoleFrame.this.update = false;
                  } else {
                     int max = ConsoleFrame.this.scrollBarModel.getMaximum() - ConsoleFrame.this.scrollBarModel.getExtent();
                     if (nv == max) {
                        ConsoleFrame.this.update = true;
                     }
                  }

                  ConsoleFrame.this.v = nv;
               }
            }
         });
         this.sp = new SearchPanel(this);
         this.ecp = new ExitCancelPanel(this);
         this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
               ConsoleFrame.this.w = ConsoleFrame.this.getWidth();
               ConsoleFrame.this.h = ConsoleFrame.this.getHeight();
               ConsoleFrame.this.sp.repaint();
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
         });
         this.setFont(font);
         this.setBackground(Color.black);
         Dimension sizes = new Dimension(this.w, this.h);
         this.setSize(sizes);
         this.setMinimumSize(sizes);
         this.setLocation(0, 0);
         this.setIconImages(TLauncherFrame.getFavicons());
         this.panel.add("Center", scrollPane);
         this.panel.add("South", this.sp);
         this.add(this.panel);
      }
   }

   public void paint(Graphics g) {
      try {
         super.paint(g);
      } catch (Exception var3) {
      }

   }

   public void update(Graphics g) {
      try {
         super.update(g);
      } catch (Exception var3) {
      }

   }

   public void println(String string) {
      this.print(string + '\n');
   }

   public void print(String string) {
      synchronized(this.busy) {
         Document document = this.textArea.getDocument();

         try {
            document.insertString(document.getLength(), string, (AttributeSet)null);
         } catch (Throwable var5) {
         }

         if (this.update) {
            this.scrollBottom();
         }

      }
   }

   public void scrollBottom() {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            ConsoleFrame.this.scrollBar.setValue(ConsoleFrame.this.scrollBar.getMaximum());
         }
      });
   }

   public String getOutput() {
      return this.c.getOutput();
   }

   public SearchPrefs getSearchPrefs() {
      return this.sp.prefs;
   }

   public void hideIn(long millis) {
      this.hiding = true;
      this.showTimer();
      AsyncThread.execute(new Runnable(millis) {
         long remaining;

         {
            this.remaining = var2;
         }

         public void run() {
            ConsoleFrame.this.ecp.setTimeout((int)this.remaining / 1000);
            U.sleepFor(1000L);

            while(ConsoleFrame.this.hiding && this.remaining > 1999L) {
               this.remaining -= 1000L;
               ConsoleFrame.this.ecp.setTimeout((int)this.remaining / 1000);
               U.sleepFor(1000L);
            }

            if (ConsoleFrame.this.hiding) {
               ConsoleFrame.this.setVisible(false);
            }

         }
      });
   }

   private void showTimer() {
      this.panel.remove(this.sp);
      this.panel.remove(this.ecp);
      this.panel.add("South", this.ecp);
      this.validate();
      this.panel.repaint();
      this.scrollBottom();
      this.toFront();
   }

   private void showSearch() {
      this.panel.remove(this.sp);
      this.panel.remove(this.ecp);
      this.panel.add("South", this.sp);
      this.validate();
      this.panel.repaint();
      this.scrollBottom();
   }

   public void cancelHiding() {
      this.hiding = false;
      this.showSearch();
   }

   public void clear() {
      this.textArea.setText("");
   }

   public void selectAll() {
      this.textArea.requestFocusInWindow();
      this.textArea.selectAll();
   }

   public void updateLocale() {
      Localizable.updateContainer(this);
      this.textpopup.updateLocale();
   }
}
