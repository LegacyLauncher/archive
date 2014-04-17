package com.turikhay.tlauncher.ui.scenes;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.swing.AnimatedVisibility;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public abstract class PseudoScene extends ExtendedLayeredPane implements
		AnimatedVisibility {
	private static final long serialVersionUID = -1L;

	private final MainPane main;
	private boolean shown = true;

	PseudoScene(MainPane main) {
		super(main);

		this.main = main;
		this.setBounds(0, 0, main.getWidth(), main.getHeight());
	}

	public MainPane getMainPane() {
		return main;
	}

	@Override
	public void setShown(boolean shown) {
		this.setShown(shown, true);
	}

	@Override
	public void setShown(boolean shown, boolean animate) {
		if (this.shown == shown)
			return;

		this.shown = shown;
		this.setVisible(shown);
	}
}
