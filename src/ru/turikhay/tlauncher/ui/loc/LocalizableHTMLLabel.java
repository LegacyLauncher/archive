package ru.turikhay.tlauncher.ui.loc;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.SwingUtil;

public class LocalizableHTMLLabel extends LocalizableLabel {
   private int labelWidth;

   public LocalizableHTMLLabel(String path, Object... vars) {
      super(path, vars);
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            LocalizableHTMLLabel.this.updateSize();
         }
      });
   }

   public LocalizableHTMLLabel(String path) {
      this(path, Localizable.EMPTY_VARS);
   }

   public LocalizableHTMLLabel() {
      this((String)null);
   }

   public int getLabelWidth() {
      return this.labelWidth;
   }

   public void setLabelWidth(int width) {
      if (width < 0) {
         throw new IllegalArgumentException();
      } else {
         this.labelWidth = width;
         this.setText(this.path, this.variables);
      }
   }

   public void setText(String path, Object... vars) {
      this.path = path;
      this.variables = Localizable.checkVariables(vars);
      StringBuilder builder = new StringBuilder();
      builder.append("<html>");
      if (this.getLabelWidth() > 0) {
         builder.append("<div width=\"").append(this.getLabelWidth()).append("\">");
      }

      builder.append(StringUtils.replace(Localizable.get(path, vars), "\n", "<br/>"));
      if (this.getLabelWidth() > 0) {
         builder.append("</div>");
      }

      builder.append("</html>");
      String rawText = builder.toString();
      this.setRawText(rawText);
   }

   public void updateSize() {
      if (this.getLabelWidth() > 0) {
         Dimension d = SwingUtil.getPreferredSize(this.getText(), true, this.getLabelWidth());
         this.setMinimumSize(d);
         this.setPreferredSize(d);
      }

   }
}
