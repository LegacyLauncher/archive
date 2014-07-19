package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public class WindowsTabbedPaneExtendedUI extends WindowsTabbedPaneUI implements ExtendedUI {
	public static final int ARC_SIZE = 16, Y_PADDING = 5;

	private CenterPanelTheme theme;

	public WindowsTabbedPaneExtendedUI(CenterPanelTheme theme) {
		this.theme = theme;
	}

	public WindowsTabbedPaneExtendedUI() {
		this(null);
	}

	@Override
	public CenterPanelTheme getTheme() {
		return theme;
	}

	@Override
	public void setTheme(CenterPanelTheme theme) {
		this.theme = theme;
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();

		contentBorderInsets = new Insets(7, 7, 7, 7);
		tabRunOverlay = 1;
		LookAndFeel.installProperty(tabPane, "opaque", Boolean.FALSE);
	}

	@Override
	protected void paintContentBorder(Graphics g0, int tabPlacement, int selectedIndex) {
		Insets insets = tabPane.getInsets();
		// Note: don't call getTabAreaInsets(), because it causes rotation.
		// Make sure "TabbedPane.tabsOverlapBorder" is set to true in WindowsLookAndFeel
		Insets tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets");
		int x = insets.left;
		int y = insets.top;
		int w = tabPane.getWidth() - insets.right - insets.left;
		int h = tabPane.getHeight() - insets.top - insets.bottom;

		// Expand area by tabAreaInsets.bottom to allow tabs to overlap onto the border.
		if (tabPlacement == LEFT || tabPlacement == RIGHT) {
			int tabWidth = calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
			if (tabPlacement == LEFT) {
				x += (tabWidth - tabAreaInsets.bottom);
			}
			w -= (tabWidth - tabAreaInsets.bottom);
		} else {
			int tabHeight = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
			if (tabPlacement == TOP) {
				y += (tabHeight - tabAreaInsets.bottom);
			}
			h -= (tabHeight - tabAreaInsets.bottom);
		}

		Graphics2D g = (Graphics2D) g0;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Color background, border;

		if(theme == null) {
			background = tabPane.getBackground();
			border = tabPane.getForeground();
		} else {
			background = theme.getPanelBackground();
			border = theme.getBorder();
		}

		g.setColor(background);
		g.fillRoundRect(x, y - Y_PADDING, w, h + Y_PADDING, ARC_SIZE, ARC_SIZE);

		g.setColor(border);
		for(int i = 1; i < 2; i++)
			g.drawRoundRect(
					x + i - 1, y + i - Y_PADDING - 1,
					w - 2*i + 1,
					h - 2*i + 1 + Y_PADDING,
					ARC_SIZE, ARC_SIZE);
	}

}
