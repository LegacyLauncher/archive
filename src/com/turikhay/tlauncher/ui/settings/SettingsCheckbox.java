package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.exceptions.ParseException;
import com.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import com.turikhay.util.StringUtil;

public class SettingsCheckbox extends LocalizableCheckbox implements SettingsField {
   private static final long serialVersionUID = -9013976214526482171L;
   private String settingspath;
   private boolean defaultState;
   private boolean saveable;

   public SettingsCheckbox(String path, String settingspath, boolean defaultState) {
      super(path);
      this.settingspath = settingspath;
      this.defaultState = defaultState;
   }

   public SettingsCheckbox(SettingsForm sf, String path, String settingspath) {
      super(path);
      this.settingspath = settingspath;
      this.defaultState = sf.global.getDefaultBoolean(settingspath);
   }

   public String getValue() {
      return String.valueOf(this.getState());
   }

   public boolean isValueValid() {
      return true;
   }

   public void setValue(String value) {
      try {
         this.setState(StringUtil.parseBoolean(value));
      } catch (ParseException var3) {
      }

   }

   public String getSettingsPath() {
      return this.settingspath;
   }

   public void setToDefault() {
      this.setState(this.defaultState);
   }

   public boolean isSaveable() {
      return this.saveable;
   }

   public void setSaveable(boolean val) {
      this.saveable = val;
   }
}
