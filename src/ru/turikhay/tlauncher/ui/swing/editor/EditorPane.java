package ru.turikhay.tlauncher.ui.swing.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.html.StyleSheet;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.util.OS;

public class EditorPane extends JEditorPane {
   private final String textColor;

   public EditorPane(Font font) {
      Color color = (new JLabel()).getForeground();
      this.textColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
      color = null;
      if (font != null) {
         this.setFont(font);
      } else {
         font = this.getFont();
      }

      StyleSheet css = new StyleSheet();
      css.importStyleSheet(this.getClass().getResource("styles.css"));
      css.addRule("body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: " + this.textColor + "; } a { color: " + this.textColor + "; text-decoration: underline; }");
      ExtendedHTMLEditorKit html = new ExtendedHTMLEditorKit();
      html.setStyleSheet(css);
      this.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
      this.setMargin(new Insets(0, 0, 0, 0));
      this.setEditorKit(html);
      this.setEditable(false);
      this.setOpaque(false);
      this.addHyperlinkListener(new HyperlinkListener() {
         public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType().equals(EventType.ACTIVATED)) {
               URL url = e.getURL();
               if (url != null) {
                  OS.openLink(url);
               }
            }

         }
      });
   }

   public EditorPane() {
      this((new LocalizableLabel()).getFont());
   }

   public EditorPane(String type, String text) {
      this();
      this.setContentType(type);
      this.setText(text);
   }
}
