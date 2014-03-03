package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.ui.block.Blockable;

public interface SettingsField extends Blockable {
   String getSettingsValue();

   void setSettingsValue(String var1);

   boolean isValueValid();
}
