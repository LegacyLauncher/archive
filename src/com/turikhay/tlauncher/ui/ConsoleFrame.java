package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
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

public class ConsoleFrame extends JFrame {
   private static final long serialVersionUID = 5667131709333334581L;
   private Font font;
   private int w = 620;
   private int h = 400;
   private int v = 0;
   private Dimension sizes;
   boolean update;
   private final JTextArea textArea;
   private final JScrollPane scrollPane;
   private final JScrollBar scrollBar;
   private final BoundedRangeModel scrollBarModel;
   private final Image favicon;
   private final StringBuilder output;

   public ConsoleFrame(String name) {
      super(name);
      this.sizes = new Dimension(this.w, this.h);
      this.update = true;
      this.favicon = TLauncher.getInstance().frame.favicon;
      this.output = new StringBuilder();
      this.font = new Font("DialogInput", 0, 14);
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
      this.setFont(this.font);
      this.setBackground(Color.black);
      this.setSize(this.sizes);
      this.setMinimumSize(this.sizes);
      this.setLocation(0, 0);
      if (this.favicon != null) {
         this.setIconImage(this.favicon);
      }

      this.add(this.scrollPane);
   }

   public void println(String string) {
      this.print(string + "\n");
   }

   public void print(String string) {
      this.output.append(string);
      Document document = this.textArea.getDocument();
      if (this.update) {
         this.scrollBottom();
      }

      try {
         document.insertString(document.getLength(), string, (AttributeSet)null);
      } catch (BadLocationException var4) {
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
      return this.output.toString();
   }
}
