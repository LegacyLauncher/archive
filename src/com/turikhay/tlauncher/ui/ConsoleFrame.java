package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.Console;
import com.turikhay.tlauncher.util.U;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

public class ConsoleFrame extends JFrame implements LocalizableComponent {
   private static final long serialVersionUID = 5667131709333334581L;
   public static final int minWidth = 620;
   public static final int minHeight = 400;
   private Font font;
   private int w = 620;
   private int h = 400;
   private int v = 0;
   private Dimension sizes;
   boolean update;
   private boolean conBusy;
   private final Panel panel;
   private final SearchPanel sp;
   private final ExitCancelPanel ecp;
   private final TextPopup textpopup;
   final JTextArea textArea;
   private final JScrollPane scrollPane;
   private final JScrollBar scrollBar;
   private final BoundedRangeModel scrollBarModel;
   private Image favicon;
   final Console c;
   boolean hiding;
   private StringBuilder output;

   public ConsoleFrame(Console c, GlobalSettings s, String name) {
      super(name);
      this.sizes = new Dimension(this.w, this.h);
      this.update = true;
      this.conBusy = false;
      this.hiding = false;
      this.output = new StringBuilder();
      if (c == null) {
         throw new NullPointerException("Console can't be NULL!");
      } else {
         this.c = c;
         LayoutManager layout = new BorderLayout();
         this.panel = new Panel(layout);
         this.font = new Font("DialogInput", 0, 14);
         this.textpopup = new TextPopup(s);
         this.textArea = new JTextArea();
         this.textArea.setLineWrap(true);
         this.textArea.setEditable(false);
         this.textArea.setMargin(new Insets(0, 0, 0, 0));
         this.textArea.setFont(this.font);
         this.textArea.setForeground(Color.white);
         this.textArea.setCaretColor(Color.white);
         this.textArea.setBackground(Color.black);
         this.textArea.setSelectionColor(Color.gray);
         this.textArea.setAutoscrolls(true);
         ((DefaultCaret)this.textArea.getCaret()).setUpdatePolicy(2);
         this.textArea.addMouseListener(new TextPopup(s));
         this.scrollPane = new JScrollPane(this.textArea);
         this.scrollPane.setBorder((Border)null);
         this.scrollPane.setVerticalScrollBarPolicy(20);
         this.scrollBar = this.scrollPane.getVerticalScrollBar();
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
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
            }

            public void componentHidden(ComponentEvent e) {
            }
         });
         if (TLauncher.getInstance().getFrame() != null) {
            this.favicon = TLauncher.getInstance().getFrame().favicon;
         }

         this.setFont(this.font);
         this.setBackground(Color.black);
         this.setSize(this.sizes);
         this.setMinimumSize(this.sizes);
         this.setLocation(0, 0);
         if (this.favicon != null) {
            this.setIconImage(this.favicon);
         }

         this.panel.add("Center", this.scrollPane);
         this.panel.add("South", this.sp);
         this.add(this.panel);
      }
   }

   public void println(String string) {
      this.print(string + "\n");
   }

   public void print(String string) {
      while(this.conBusy) {
         U.sleepFor(10L);
      }

      this.conBusy = true;
      this.output.append(string);
      Document document = this.textArea.getDocument();
      if (this.update) {
         this.scrollBottom();
      }

      try {
         document.insertString(document.getLength(), string, (AttributeSet)null);
      } catch (BadLocationException var4) {
      }

      this.conBusy = false;
   }

   public void scrollBottom() {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            ConsoleFrame.this.scrollBar.setValue(ConsoleFrame.this.scrollBar.getMaximum());
         }
      });
   }

   public String getOutput() {
      while(this.conBusy) {
         U.sleepFor(10L);
      }

      this.conBusy = true;
      String s = this.output.toString();
      this.conBusy = false;
      return s;
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
      this.panel.validate();
      this.scrollBottom();
      this.toFront();
   }

   private void showSearch() {
      this.panel.remove(this.sp);
      this.panel.remove(this.ecp);
      this.panel.add("South", this.sp);
      this.panel.validate();
      this.scrollBottom();
   }

   public void cancelHiding() {
      this.hiding = false;
      this.showSearch();
   }

   public void clear() {
      this.textArea.setText("");
      this.output = new StringBuilder();
   }

   public void selectAll() {
      this.textArea.requestFocusInWindow();
      this.textArea.selectAll();
   }

   public void updateLocale() {
      TLauncherFrame.updateContainer(this, true);
      this.textpopup.updateLocale();
   }
}
