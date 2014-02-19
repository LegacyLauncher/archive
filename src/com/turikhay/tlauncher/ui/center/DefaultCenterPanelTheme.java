package com.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class DefaultCenterPanelTheme extends CenterPanelTheme {
	public final Color backgroundColor = new Color(255, 255, 255, 255); // White
	public final Color panelBackgroundColor = new Color(255, 255, 255, 128); // Half-white
	
	public final Color focusColor = new Color(0, 0, 0, 255); // Black
	public final Color focusLostColor = new Color(128, 128, 128, 255); // Gray
	
	public final Color successColor = Color.getHSBColor(0.25F, 0.66F, 0.66F); // Bright green
	public final Color failureColor = Color.getHSBColor(0F, 0.3F, 1F); // Pink
	
	public final Color borderColor = successColor;
	public final Color delPanelColor = successColor;
	
	@Override
	public Color getBackground() {
		return backgroundColor;
	}
	
	@Override
	public Color getPanelBackground() {
		return panelBackgroundColor;
	}
	
	@Override
	public Color getFocus() {
		return focusColor;
	}
	
	@Override
	public Color getFocusLost() {
		return focusLostColor;
	}
	
	@Override
	public Color getSuccess() {
		return successColor;
	}
	
	@Override
	public Color getFailure() {
		return failureColor;
	}
	
	@Override
	public Color getBorder() {
		return borderColor;
	}
	
	@Override
	public Color getDelPanel() {
		return delPanelColor;
	}
}
