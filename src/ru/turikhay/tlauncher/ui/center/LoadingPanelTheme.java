package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class LoadingPanelTheme extends DefaultCenterPanelTheme {
	protected final Color panelBackgroundColor = new Color(255, 255, 255, 168); // Half-white

	@Override
	public Color getPanelBackground() {
		return panelBackgroundColor;
	}
}
