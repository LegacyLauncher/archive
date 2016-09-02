package ru.turikhay.tlauncher.ui.images;

import javax.swing.*;

public interface ExtendedIcon extends Icon {
    DisabledImageIcon getDisabledInstance();

    void setIconSize(int width, int height);
}
