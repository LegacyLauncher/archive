package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedFrame;

public abstract class ActionFrame extends ExtendedFrame implements LocalizableComponent {
   private String title;
   private Object[] titleVars;

   public final void setTitlePath(String title, Object... vars) {
      this.title = title;
      this.titleVars = vars;
      this.updateTitle();
   }

   public void updateLocale() {
      this.updateTitle();
      Localizable.updateContainer(this);
   }

   private void updateTitle() {
      this.setTitle(Localizable.get(this.title, this.titleVars));
   }
}
