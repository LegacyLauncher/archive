package ru.turikhay.tlauncher.ui.alert;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

class AlertPanel extends JPanel {
   private static final int MAX_CHARS = 80;
   private static final int MAX_WIDTH = 500;
   private static final int MAX_HEIGHT = 300;
   private static final Dimension MAX_SIZE = new Dimension(500, 300);

   AlertPanel(String rawMessage, Object rawTextarea) {
      this.setLayout(new BoxLayout(this, 1));
      String message;
      if (rawMessage == null) {
         message = null;
      } else {
         message = StringUtil.wrap((String)("<html>" + rawMessage + "</html>"), 80);
      }

      EditorPane label = new EditorPane("text/html", message);
      label.setAlignmentX(0.0F);
      label.setFocusable(false);
      this.add(label);
      if (rawTextarea != null) {
         String textarea = U.toLog(rawTextarea);
         JTextArea area = new JTextArea(textarea);
         area.addMouseListener(new TextPopup());
         area.setFont(this.getFont());
         area.setEditable(false);
         ScrollPane scroll = new ScrollPane(area, true);
         scroll.setAlignmentX(0.0F);
         scroll.setVBPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
         int textAreaHeight = StringUtil.countLines(textarea) * this.getFontMetrics(this.getFont()).getHeight();
         if (textAreaHeight > 300) {
            scroll.setPreferredSize(MAX_SIZE);
         }

         this.add(scroll);
      }
   }
}
