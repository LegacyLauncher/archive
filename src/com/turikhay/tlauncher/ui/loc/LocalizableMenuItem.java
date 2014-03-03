package com.turikhay.tlauncher.ui.loc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenuItem;

public class LocalizableMenuItem extends JMenuItem implements LocalizableComponent {
   private static final long serialVersionUID = 1364363532569997394L;
   private static List items = Collections.synchronizedList(new ArrayList());
   private String path;

   public LocalizableMenuItem(String path) {
      this.init(path);
   }

   private void init(String path) {
      this.path = path;
      this.setText(path);
      items.add(this);
   }

   public void setText(String path) {
      this.path = path;
      super.setText(Localizable.exists() ? Localizable.get(path) : path);
   }

   public void updateLocale() {
      this.setText(this.path);
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
