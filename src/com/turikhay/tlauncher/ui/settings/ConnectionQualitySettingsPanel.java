package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.block.BlockablePanel;
import javax.swing.BoxLayout;

public class ConnectionQualitySettingsPanel extends BlockablePanel implements SettingsField {
   private static final long serialVersionUID = -9108973380914818944L;
   private final String path = "connection";
   private final String defaultValue = Configuration.ConnectionQuality.getDefault().toString();
   final SettingsRadioGroup connGroup;

   ConnectionQualitySettingsPanel(SettingsForm sf) {
      this.setOpaque(false);
      this.setLayout(new BoxLayout(this, 0));
      this.connGroup = new SettingsRadioGroup(this.path, new SettingsRadioButton[]{sf.goodQuality, sf.normalQuality, sf.badQuality});
      this.add(sf.goodQuality);
      this.add(sf.normalQuality);
      this.add(sf.badQuality);
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }

   public String getSettingsPath() {
      return this.connGroup.getSettingsPath();
   }

   public String getValue() {
      String value = this.connGroup.getValue();
      return value == null ? this.defaultValue : value;
   }

   public boolean isValueValid() {
      return true;
   }

   public void setValue(String value) {
      this.connGroup.selectValue(value);
   }

   public void setToDefault() {
      this.connGroup.selectValue(this.defaultValue);
   }
}
