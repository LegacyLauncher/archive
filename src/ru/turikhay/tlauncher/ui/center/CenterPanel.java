package ru.turikhay.tlauncher.ui.center;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.UnblockablePanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.U;

public class CenterPanel extends BlockablePanel {
	private static final long serialVersionUID = -1975869198322761508L;

	public static final CenterPanelTheme
	defaultTheme = new DefaultCenterPanelTheme(),
	tipTheme = new TipPanelTheme(),
	loadingTheme = new LoadingPanelTheme(),
	settingsTheme = new SettingsPanelTheme();

	public static final Insets
	defaultInsets = new Insets(5, 24, 18, 24),
	squareInsets = new Insets(15, 15, 15, 15),
	smallSquareInsets = new Insets(7, 7, 7, 7),
	smallSquareNoTopInsets = new Insets(5, 15, 5, 15),
	noInsets = new Insets(0, 0, 0, 0);

	protected final static int ARC_SIZE = 32;

	private final Insets insets;
	private final CenterPanelTheme theme;

	protected final ExtendedPanel messagePanel;
	protected final LocalizableLabel messageLabel;

	public final TLauncher tlauncher;
	public final Configuration global;
	public final LangConfiguration lang;

	public CenterPanel() {
		this(null, null);
	}

	public CenterPanel(Insets insets) {
		this(null, insets);
	}

	public CenterPanel(CenterPanelTheme theme) {
		this(theme, null);
	}

	public CenterPanel(CenterPanelTheme theme, Insets insets) {
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
		messageLabel.setAlignmentX(CENTER_ALIGNMENT);

		this.messagePanel = new VPanel();
		messagePanel.setAlignmentX(CENTER_ALIGNMENT);
		messagePanel.setInsets(new Insets(3, 0, 3, 0));
		messagePanel.add(messageLabel);
	}

	@Override
	public void paintComponent(Graphics g0) {		
		Graphics2D g = (Graphics2D) g0;

		int x = 0;

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(getBackground());
		g.fillRoundRect(x, x, getWidth(), getHeight(), ARC_SIZE, ARC_SIZE);

		g.setColor(theme.getBorder());
		for(x = 1; x < 2; x++)
			g.drawRoundRect(x - 1, x - 1,
					getWidth() - 2*x + 1,
					getHeight() - 2*x + 1,
					ARC_SIZE, ARC_SIZE);

		Color shadow = U.shiftAlpha(Color.gray, -155);

		for(x = 2; ;x++) {
			shadow = U.shiftAlpha(shadow, -10);

			if(shadow.getAlpha() == 0)
				break;

			g.setColor(shadow);
			g.drawRoundRect(x - 1, x - 1,
					getWidth() - 2*x + 1,
					getHeight() - 2*x + 1,
					ARC_SIZE - 2*x + 1, ARC_SIZE - 2*x + 1);
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

	protected Del del(int aligment, int width, int height) {
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

	protected boolean setMessage(String message, Object...vars) {
		this.messageLabel.setForeground(theme.getFocus());
		this.messageLabel
		.setText(message == null || message.length() == 0 ? " "
				: message, vars);
		return true;
	}

	protected boolean setMessage(String message) {
		return setMessage(message, Localizable.EMPTY_VARS);
	}

	public static BlockablePanel sepPan(LayoutManager manager, Component... components) {
		BlockablePanel panel = new BlockablePanel(manager) {

			private static final long serialVersionUID = 1L;

			@Override
			public Insets getInsets() {
				return noInsets;
			}

		};
		panel.add(components);

		return panel;
	}

	public static BlockablePanel sepPan(Component... components) {
		return sepPan(new GridLayout(0, 1), components);
	}

	public static UnblockablePanel uSepPan(LayoutManager manager, Component... components) {
		UnblockablePanel panel = new UnblockablePanel(manager) {

			private static final long serialVersionUID = 1L;

			@Override
			public Insets getInsets() {
				return noInsets;
			}

		};
		panel.add(components);

		return panel;
	}

	public static UnblockablePanel uSepPan(Component... components) {
		return uSepPan(new GridLayout(0, 1), components);
	}

	protected void log(Object... o) {
		U.log("[" + getClass().getSimpleName() + "]", o);
	}
}
