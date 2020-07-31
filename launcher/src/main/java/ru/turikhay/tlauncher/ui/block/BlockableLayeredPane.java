package ru.turikhay.tlauncher.ui.block;

import javax.swing.*;

public class BlockableLayeredPane extends JLayeredPane implements Blockable {
    private static final long serialVersionUID = 1L;

    public void block(Object reason) {
        Blocker.blockComponents(this, reason);
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(this, reason);
    }
}
