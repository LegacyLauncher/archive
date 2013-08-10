package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.util.U;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

public class ConsoleFrame extends JFrame {
   private static final long serialVersionUID = 5667131709333334581L;
   private Font font;
   private int fontsize;
   private boolean scroll = true;
   private Dimension sizes = new Dimension(620, 400);
   private final JTextArea textArea;
   private final JScrollPane scrollPane;
   private final Image favicon;
   private final StringBuilder output;

   public ConsoleFrame(String name) {
      super(name);
      this.favicon = TLauncher.getInstance().frame.favicon;
      this.output = new StringBuilder();
      this.font = new Font("DialogInput", 0, 14);
      this.fontsize = this.font.getSize();
      U.log(this.fontsize);
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
      this.textArea.addCaretListener(new CaretListener() {
         public void caretUpdate(CaretEvent e) {
            int d = e.getDot();
            int m = e.getMark();
            ConsoleFrame.this.scroll = d == m;
         }
      });
      this.scrollPane = new JScrollPane(this.textArea);
      this.scrollPane.setBorder((Border)null);
      this.scrollPane.setVerticalScrollBarPolicy(20);
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
      JScrollBar scrollBar = this.scrollPane.getVerticalScrollBar();
      if (this.scroll) {
         scrollBar.setValue(scrollBar.getMaximum());
      }

      try {
         document.insertString(document.getLength(), string, (AttributeSet)null);
      } catch (BadLocationException var5) {
      }

   }
}
