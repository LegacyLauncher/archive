package net.legacylauncher.ui.block;

import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.swing.extended.ExtendedButton;


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
