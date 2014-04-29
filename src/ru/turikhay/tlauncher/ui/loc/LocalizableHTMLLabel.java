package ru.turikhay.tlauncher.ui.loc;

public class LocalizableHTMLLabel extends LocalizableLabel {
   private static final long serialVersionUID = 4451490831936280751L;

   public LocalizableHTMLLabel(String path, Object... vars) {
      super(path, vars);
   }

   public LocalizableHTMLLabel(String path) {
      super(path);
   }

   public LocalizableHTMLLabel() {
   }

   public LocalizableHTMLLabel(int horizontalAlignment) {
      super(horizontalAlignment);
   }

   public void setText(String path, Object... vars) {
      this.path = path;
      this.variables = Localizable.checkVariables(vars);
      String value = Localizable.get(path);
      if (value != null) {
         value = "<html>" + value.replace("\n", "<br/>") + "</html>";

         for(int i = 0; i < this.variables.length; ++i) {
            value = value.replace("%" + i, this.variables[i]);
         }
      }

      this.setRawText(value);
   }
}
