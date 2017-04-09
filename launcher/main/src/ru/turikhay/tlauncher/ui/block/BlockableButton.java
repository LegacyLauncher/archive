package ru.turikhay.tlauncher.ui.block;

import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;


public class BlockableButton extends ExtendedButton implements Blockable {

    public BlockableButton(ImageIcon icon) {
        super(icon);
    }

    @Override
    public void block(Object var1) {
        setEnabled(false);
    }

    @Override
    public void unblock(Object var1) {
        setEnabled(true);
    }
}
