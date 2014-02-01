package com.turikhay.tlauncher.ui;

import com.turikhay.util.StringUtil;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

public class AlertPanel extends Panel {
   private static final long serialVersionUID = -8032765825488193573L;
   private final JLabel label;

   public AlertPanel(String message) {
      LayoutManager lm = new BoxLayout(this, 1);
      this.setLayout(lm);
      this.label = new JLabel(message);
      this.label.setAlignmentX(0.5F);
      this.add(this.label);
   }

   public void addTextArea(String text) {
      JTextArea area = new JTextArea(text);
      area.setFont(this.getFont());
      area.setLineWrap(true);
      area.setMargin(new Insets(0, 0, 0, 0));
      area.setCaretPosition(0);
      area.setAlignmentX(0.5F);
      area.setEditable(false);
      area.addMouseListener(new TextPopup());
      JScrollPane scroll = new JScrollPane(area);
      scroll.setBorder((Border)null);
      scroll.setVerticalScrollBarPolicy(20);
      int lineHeight = area.getFont() != null ? this.getFontMetrics(area.getFont()).getHeight() : 15;
      int height = StringUtil.countLines(text) * lineHeight;
      int width = 300;
      if (height > 150) {
         width = 400;
         height = 200;
      }

      this.label.setMinimumSize(new Dimension(width, 0));
      scroll.setPreferredSize(new Dimension(width, height));
      this.add(scroll);
   }
}
