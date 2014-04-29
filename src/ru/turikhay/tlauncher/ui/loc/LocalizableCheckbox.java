package ru.turikhay.tlauncher.ui.loc;

import java.awt.event.ItemListener;
import javax.swing.JCheckBox;

public class LocalizableCheckbox extends JCheckBox implements LocalizableComponent {
   private static final long serialVersionUID = 1L;
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

   public String getLangPath() {
      return this.path;
   }

   public boolean getState() {
      return super.getModel().isSelected();
   }

   public void setState(boolean state) {
      super.getModel().setSelected(state);
   }

   public void addListener(ItemListener l) {
      super.getModel().addItemListener(l);
   }

   public void removeListener(ItemListener l) {
      super.getModel().removeItemListener(l);
   }

   public void updateLocale() {
      this.setLabel(this.path);
   }

   private void init() {
      this.setFont(this.getFont().deriveFont(12.0F));
      this.setOpaque(false);
   }
}
