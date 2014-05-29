package ru.turikhay.tlauncher.ui.alert;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.extended.EditorPane;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

class AlertPanel extends JPanel {
   private static final long serialVersionUID = -8032765825488193573L;
   private static final int MAX_CHARS_ON_LINE = 70;
   private static final int MAX_WIDTH = 500;
   private static final int MAX_HEIGHT = 300;

   AlertPanel(String message, Object textarea) {
      LayoutManager lm = new BoxLayout(this, 1);
      this.setLayout(lm);
      String textareaContent = textarea == null ? null : U.w(U.toLog(textarea), 70, true);
      String messageContent = message == null ? null : "<html>" + U.w(message, 70).replace("\n", "<br/>") + "<br/></html>";
      Dimension maxSize = new Dimension(500, 300);
      EditorPane label = new EditorPane("text/html", messageContent);
      label.setFocusable(false);
      label.setAlignmentX(0.0F);
      this.add(label);
      if (textareaContent != null) {
         JTextArea area = new JTextArea(textareaContent);
         area.setAlignmentX(0.0F);
         area.setMaximumSize(maxSize);
         area.addMouseListener(new TextPopup());
         area.setFont(this.getFont());
         area.setEditable(false);
         JScrollPane scroll = new JScrollPane(area);
         scroll.setAlignmentX(0.0F);
         scroll.setMaximumSize(maxSize);
         scroll.setVerticalScrollBarPolicy(20);
         int textAreaHeight = StringUtil.countLines(textareaContent) * this.getFontMetrics(this.getFont()).getHeight();
         if (textAreaHeight > 300) {
            scroll.setPreferredSize(maxSize);
         }

         this.add(scroll);
      }
   }
}
