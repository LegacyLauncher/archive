package com.turikhay.tlauncher.ui.center;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.swing.Del;
import com.turikhay.util.U;

public class CenterPanel extends BlockablePanel {
	private static final long serialVersionUID = -1975869198322761508L;

	public static final CenterPanelTheme defaultTheme = new DefaultCenterPanelTheme();
	protected static final CenterPanelTheme tipTheme = new TipPanelTheme();

	private static final Insets defaultInsets = new Insets(5, 24, 18, 24);
	protected static final Insets squareInsets = new Insets(15, 15, 15, 15);
	protected static final Insets smallSquareInsets = new Insets(7, 7, 7, 7);
	
	protected final static int ARC_SIZE = 32;

	private final Insets insets;
	private final CenterPanelTheme theme;

	protected final JPanel messagePanel;
	private final LocalizableLabel messageLabel;

	public final TLauncher tlauncher;
	public final Configuration global;
	public final LangConfiguration lang;

	protected CenterPanel() {
		this(null, null);
	}

	protected CenterPanel(Insets insets) {
		this(null, insets);
	}

	protected CenterPanel(CenterPanelTheme theme, Insets insets) {
		this.tlauncher = TLauncher.getInstance();
		global = tlauncher.getSettings();
		lang = tlauncher.getLang();

		this.theme = theme = (theme == null) ? defaultTheme : theme;
		this.insets = insets = (insets == null) ? defaultInsets : insets;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(theme.getPanelBackground());

		this.messageLabel = new LocalizableLabel("  ");
		messageLabel.setFont(getFont().deriveFont(Font.BOLD));
		messageLabel.setVerticalAlignment(SwingConstants.CENTER);
		messageLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		this.messagePanel = sepPan(new FlowLayout(), messageLabel);

		add(Box.createVerticalGlue());
	}

	@Override
	public void paintComponent(Graphics g0) {		
		Graphics2D g = (Graphics2D) g0;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(getBackground());
		g.fillRoundRect(0, 0, getWidth(), getHeight(), ARC_SIZE, ARC_SIZE);

		g.setColor(theme.getBorder());
		for(int x = 1; x < 2; x++) {
			g.drawRoundRect(x - 1, x - 1, getWidth() - 2 * x + 1, getHeight()
					- 2 * x + 1, ARC_SIZE, ARC_SIZE);
		}

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);

		super.paintComponent(g);
	}

	public CenterPanelTheme getTheme() {
		return theme;
	}

	@Override
	public Insets getInsets() {
		return insets;
	}

	protected Del del(int aligment) {
		return new Del(1, aligment, theme.getBorder());
	}

	public Del del(int aligment, int width, int height) {
		return new Del(1, aligment, width, height, theme.getBorder());
	}

	public void defocus() {
		this.requestFocusInWindow();
	}

	public boolean setError(String message) {
		this.messageLabel.setForeground(theme.getFailure());
		this.messageLabel
				.setText(message == null || message.length() == 0 ? " "
						: message);
		return false;
	}

	protected boolean setMessage(String message) {
		this.messageLabel.setForeground(theme.getFocus());
		this.messageLabel
				.setText(message == null || message.length() == 0 ? " "
						: message);
		return true;
	}

	public static JPanel sepPan(LayoutManager manager, Component... components) {
		BlockablePanel panel = new BlockablePanel(manager);
		panel.add(components);

		return panel;
	}

	public static JPanel sepPan(Component... components) {
		return sepPan(new GridLayout(0, 1), components);
	}

	protected void log(Object... o) {
		U.log("[" + getClass().getSimpleName() + "]", o);
	}
}
