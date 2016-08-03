package ru.turikhay.tlauncher.ui.alert;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedTextArea;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

class AlertPanel extends ExtendedPanel {
   AlertPanel(String text, Object content) {
      int width = SwingUtil.magnify(600);
      this.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = 21;
      c.weightx = 1.0D;
      c.gridy = -1;
      ++c.gridy;
      this.add(Box.createRigidArea(new Dimension(width, 1)), c);
      EditorPane label = new EditorPane("text/html", "<html><div width=\"" + width + "\">" + StringUtils.replace(text, "\n", "<br/>") + "</div></html>");
      ++c.gridy;
      this.add(label, c);
      if (content != null) {
         ++c.gridy;
         this.add(Box.createRigidArea(new Dimension(width, 5)), c);
         String strContent = U.toLog(content);
         ExtendedTextArea textarea = new ExtendedTextArea();
         textarea.setWrapStyleWord(false);
         textarea.setLineWrap(true);
         textarea.setText(strContent);
         textarea.addMouseListener(new TextPopup());
         textarea.setEditable(false);
         ScrollPane scroll = new ScrollPane(textarea, true);
         scroll.setPreferredSize(getPrefSize(strContent, width, width / 2, textarea.getFontMetrics(textarea.getFont()), textarea.getInsets()));
         ++c.gridy;
         this.add(scroll, c);
      }

   }

   private static Dimension getPrefSize(String str, int prefWidth, int maxHeight, FontMetrics metrics, Insets insets) {
      if (str != null && str.length() != 0) {
         int len = str.length();
         int lines = 1;
         int lineWidth = 0;

         int pos;
         for(pos = 0; pos < len; ++pos) {
            char c = str.charAt(pos);
            lineWidth += metrics.charWidth(c);
            if (lineWidth <= prefWidth && c != '\n') {
               if (c != '\r' || pos + 1 >= len || str.charAt(pos + 1) != '\n') {
                  continue;
               }

               ++pos;
               if (pos <= 0) {
                  continue;
               }
            }

            ++lines;
            lineWidth = 0;
         }

         pos = metrics.getHeight() * lines + SwingUtil.magnify(2);
         if (pos > maxHeight) {
            pos = maxHeight;
         }

         pos += insets.top + insets.bottom;
         return new Dimension(prefWidth, pos);
      } else {
         return new Dimension(0, 0);
      }
   }
}
