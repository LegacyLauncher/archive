package ru.turikhay.tlauncher.ui.loc;

import javax.swing.JCheckBox;
import ru.turikhay.tlauncher.ui.TLauncherFrame;

public class LocalizableCheckbox extends JCheckBox implements LocalizableComponent {
   private String path;

   public LocalizableCheckbox(String path) {
      this.init();
      this.setLabel(path);
   }

   public LocalizableCheckbox(String path, boolean state) {
      super("", state);
      this.init();
      this.setText(path);
   }

   /** @deprecated */
   @Deprecated
   public void setLabel(String path) {
      this.setText(path);
   }

   public void setText(String path) {
      this.path = path;
      super.setText(Localizable.get() == null ? path : Localizable.get().get(path));
   }

   public boolean getState() {
      return super.getModel().isSelected();
   }

   public void updateLocale() {
      this.setLabel(this.path);
   }

   private void init() {
      this.setFont(this.getFont().deriveFont(TLauncherFrame.getFontSize()));
      this.setOpaque(false);
   }
}
