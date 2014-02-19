package com.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class TipPanelTheme extends DefaultCenterPanelTheme {
	public final Color panelBackgroundColor = new Color(255, 255, 255, 168); // Half-white
	public final Color borderColor = failureColor;
	
	@Override
	public Color getPanelBackground() {
		return panelBackgroundColor;
	}
	
	@Override
	public Color getBorder() {
		return borderColor;
	}

}
