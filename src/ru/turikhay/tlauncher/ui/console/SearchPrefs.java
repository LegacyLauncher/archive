package ru.turikhay.tlauncher.ui.console;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class SearchPrefs extends JPanel {
   private static final long serialVersionUID = -5187427203445160236L;
   private LocalizableCheckbox pcase;
   private LocalizableCheckbox pwhole;
   private LocalizableCheckbox pcycle;
   private LocalizableCheckbox pregexp;

   SearchPrefs(SearchPanel sp) {
      LayoutManager layout = new BoxLayout(this, 2);
      this.setLayout(layout);
      this.setBackground(Color.black);
      this.setForeground(Color.white);
      this.add(this.pcase = new LocalizableCheckbox("console.search.prefs.case"));
      this.add(this.pwhole = new LocalizableCheckbox("console.search.prefs.whole"));
      this.add(this.pcycle = new LocalizableCheckbox("console.search.prefs.cycle"));
      this.add(this.pregexp = new LocalizableCheckbox("console.search.prefs.regexp"));
      Component[] var6;
      int var5 = (var6 = this.getComponents()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Component c = var6[var4];
         c.setForeground(Color.white);
      }

   }

   public boolean isCaseSensetive() {
      return this.pcase.getState();
   }

   public boolean isWordSearch() {
      return this.pwhole.getState();
   }

   public boolean isCycled() {
      return this.pcycle.getState();
   }

   public boolean isRegExp() {
      return this.pregexp.getState();
   }

   public void setCaseSensetive(boolean s) {
      this.pcase.setState(s);
   }

   public void setWordSearch(boolean s) {
      this.pwhole.setState(s);
   }

   public void setCycled(boolean s) {
      this.pcycle.setState(s);
   }

   public void setRegExp(boolean s) {
      this.pregexp.setState(s);
   }

   public boolean[] get() {
      return new boolean[]{this.isCaseSensetive(), this.isWordSearch(), this.isCycled(), this.isRegExp()};
   }
}
