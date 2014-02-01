package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.ui.block.BlockablePanel;
import javax.swing.BoxLayout;

public class ConsoleSettingsPanel extends BlockablePanel implements SettingsField {
   private static final long serialVersionUID = -9108973380914818944L;
   private final String path = "gui.console";
   private final String defaultValue = GlobalSettings.ConsoleType.getDefault().toString();
   final SettingsRadioGroup consoleGroup;

   ConsoleSettingsPanel(SettingsForm sf) {
      this.setOpaque(false);
      this.setLayout(new BoxLayout(this, 0));
      this.consoleGroup = new SettingsRadioGroup(this.path, new SettingsRadioButton[]{sf.globalConsole, sf.minecraftConsole, sf.noneConsole});
      this.add(sf.globalConsole);
      this.add(sf.minecraftConsole);
      this.add(sf.noneConsole);
   }

   public void block(Object reason) {
      this.setEnabled(false);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
   }

   public String getSettingsPath() {
      return this.consoleGroup.getSettingsPath();
   }

   public String getValue() {
      String value = this.consoleGroup.getValue();
      return value == null ? this.defaultValue : value;
   }

   public boolean isValueValid() {
      return true;
   }

   public void setValue(String value) {
      this.consoleGroup.selectValue(value);
   }

   public void setToDefault() {
      this.consoleGroup.selectValue(this.defaultValue);
   }
}
