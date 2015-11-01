package ru.turikhay.tlauncher.ui.versions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.HTMLLabel;
import ru.turikhay.util.OS;

public class VersionTipPanel extends CenterPanel implements LocalizableComponent, ResizeableComponent {
   private final HTMLLabel tip = new HTMLLabel();

   VersionTipPanel(VersionHandler handler) {
      super(CenterPanel.tipTheme, CenterPanel.squareInsets);
      this.add(this.tip);
      this.tip.addPropertyChangeListener("html", new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent evt) {
            Object o = evt.getNewValue();
            if (o != null && o instanceof View) {
               View view = (View)o;
               BasicHTML.getHTMLBaseline(view, 300 - VersionTipPanel.this.getHorizontalInsets(), 0);
            }

         }
      });
      this.updateLocale();
   }

   public void updateLocale() {
      this.tip.setText("");
      String text = Localizable.get("version.list.tip");
      if (text != null) {
         text = text.replace("{Ctrl}", OS.OSX.isCurrent() ? "Command" : "Ctrl");
         this.tip.setText(text);
         this.onResize();
      }

   }

   public void onResize() {
      this.setSize(300, this.tip.getHeight() + this.getVerticalInsets());
   }

   private int getVerticalInsets() {
      return this.getInsets().top + this.getInsets().bottom;
   }

   private int getHorizontalInsets() {
      return this.getInsets().left + this.getInsets().right;
   }
}
