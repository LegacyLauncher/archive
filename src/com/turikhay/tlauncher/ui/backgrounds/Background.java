package com.turikhay.tlauncher.ui.backgrounds;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.swing.AnimatedVisibility;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public abstract class Background extends ExtendedLayeredPane implements AnimatedVisibility, AnimatedBackground {
	private static final long serialVersionUID = -1353975966057230209L;
	
	public Background(MainPane main) {
		super(main);
	}

	@Override
	public void setShown(boolean shown) {
		this.setShown(shown, true);
	}

	@Override
	public void setShown(boolean shown, boolean animate) {
		this.setVisible(shown);
		if(shown) this.start(); else this.stop();
	}
}
