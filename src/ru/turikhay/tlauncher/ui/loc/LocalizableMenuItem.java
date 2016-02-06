package ru.turikhay.tlauncher.ui.loc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;

public class LocalizableMenuItem extends JMenuItem implements LocalizableComponent {
   private static List items = Collections.synchronizedList(new ArrayList());
   private String path;
   private String[] variables;

   public LocalizableMenuItem(String path, Object... vars) {
      items.add(this);
      this.setText(path, vars);
   }

   public LocalizableMenuItem(String path) {
      this(path, Localizable.EMPTY_VARS);
   }

   public void setText(String path, Object... vars) {
      this.path = path;
      this.variables = Localizable.checkVariables(vars);
      String value = Localizable.get(path);

      for(int i = 0; i < this.variables.length; ++i) {
         value = value.replace("%" + i, this.variables[i]);
      }

      super.setText(value);
   }

   public void setText(String path) {
      this.setText(path, Localizable.EMPTY_VARS);
   }

   public void setVariables(Object... vars) {
      this.setText(this.path, vars);
   }

   public void updateLocale() {
      this.setText(this.path, this.variables);
   }

   public static void updateLocales() {
      Iterator var1 = items.iterator();

      while(var1.hasNext()) {
         LocalizableMenuItem item = (LocalizableMenuItem)var1.next();
         if (item != null) {
            item.updateLocale();
         }
      }

   }
}
