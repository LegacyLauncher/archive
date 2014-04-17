package com.turikhay.tlauncher.ui.center;

import java.awt.Color;

public class DefaultCenterPanelTheme extends CenterPanelTheme {
	private final Color backgroundColor = new Color(255, 255, 255, 255); // White
	private final Color panelBackgroundColor = new Color(255, 255, 255, 168); // Half-white

	private final Color focusColor = new Color(0, 0, 0, 255); // Black
	private final Color focusLostColor = new Color(128, 128, 128, 255); // Gray

	// //Color.getHSBColor(0.25F, 0.66F, 0.66F);
	private final Color successColor = new Color(78, 196, 78, 255); // Green
	final Color failureColor = Color.getHSBColor(0F, 0.3F, 1F); // Pink

	private final Color borderColor = new Color(28, 128, 28, 255); // Dark green
	private final Color delPanelColor = successColor;

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
