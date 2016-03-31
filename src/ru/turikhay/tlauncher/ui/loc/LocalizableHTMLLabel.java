package ru.turikhay.tlauncher.ui.loc;

import org.apache.commons.lang3.StringUtils;

public class LocalizableHTMLLabel extends LocalizableLabel {
   public LocalizableHTMLLabel(String path, Object... vars) {
      super(path, vars);
   }

   public LocalizableHTMLLabel(String path) {
      this(path, Localizable.EMPTY_VARS);
   }

   public LocalizableHTMLLabel() {
      this((String)null);
   }

   public void setText(String path, Object... vars) {
      this.path = path;
      this.variables = Localizable.checkVariables(vars);
      this.setRawText("<html>" + StringUtils.replaceChars(Localizable.get(path, vars), "\n", "<br/>") + "</html>");
   }
}
