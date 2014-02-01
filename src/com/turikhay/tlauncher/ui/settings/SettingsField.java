package com.turikhay.tlauncher.ui.settings;

public interface SettingsField {
   String getSettingsPath();

   String getValue();

   boolean isValueValid();

   void setValue(String var1);

   void setToDefault();
}
