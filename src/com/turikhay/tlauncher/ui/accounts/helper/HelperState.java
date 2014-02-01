package com.turikhay.tlauncher.ui.accounts.helper;

import com.turikhay.tlauncher.ui.loc.LocalizableMenuItem;

public enum HelperState {
   LICENSE,
   PIRATE,
   HELP(false),
   NONE;

   public final LocalizableMenuItem item;
   public final boolean showInList;

   private HelperState() {
      this(true);
   }

   private HelperState(boolean showInList) {
      this.item = new LocalizableMenuItem("auth.helper." + this.toString());
      this.showInList = showInList;
   }

   public String toString() {
      return super.toString().toLowerCase();
   }
}
