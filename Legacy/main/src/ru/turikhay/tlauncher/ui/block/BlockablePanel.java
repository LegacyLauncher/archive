package ru.turikhay.tlauncher.ui.block;

import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import java.awt.*;

public class BlockablePanel extends ExtendedPanel implements Blockable {
    private static final long serialVersionUID = 1L;

    public BlockablePanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public BlockablePanel(LayoutManager layout) {
        super(layout);
    }

    public BlockablePanel() {
        super();
    }

    public void block(Object reason) {
        Blocker.blockComponents(this, reason);
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(this, reason);
    }
}
