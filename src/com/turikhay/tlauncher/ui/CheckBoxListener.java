package com.turikhay.tlauncher.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class CheckBoxListener implements ItemListener {
   public void itemStateChanged(ItemEvent e) {
      this.itemStateChanged(e.getStateChange() == 1);
   }

   public abstract void itemStateChanged(boolean var1);
}
