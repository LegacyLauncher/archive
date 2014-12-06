package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class TipPanelTheme extends DefaultCenterPanelTheme {
	private final Color borderColor = failureColor;

	@Override
	public Color getBorder() {
		return borderColor;
	}

}
