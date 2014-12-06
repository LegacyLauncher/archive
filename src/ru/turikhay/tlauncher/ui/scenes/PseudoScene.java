package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.swing.AnimatedVisibility;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public abstract class PseudoScene extends ExtendedLayeredPane implements
		AnimatedVisibility {
	private static final long serialVersionUID = -1L;

	private final MainPane main;
	private boolean shown = true;

	PseudoScene(MainPane main) {
		super(main);

		this.main = main;
		this.setSize(main.getWidth(), main.getHeight());
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
