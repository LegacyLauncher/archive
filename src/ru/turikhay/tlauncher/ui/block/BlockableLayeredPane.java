package ru.turikhay.tlauncher.ui.block;

import javax.swing.JLayeredPane;

public class BlockableLayeredPane extends JLayeredPane implements Blockable {
	private static final long serialVersionUID = 1L;

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(this, reason);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(this, reason);
	}
}
