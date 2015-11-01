package ru.turikhay.tlauncher.ui.images;

import javax.swing.Icon;

public interface ExtendedIcon extends Icon {
   DisabledImageIcon getDisabledInstance();

   void setIconSize(int var1, int var2);
}
