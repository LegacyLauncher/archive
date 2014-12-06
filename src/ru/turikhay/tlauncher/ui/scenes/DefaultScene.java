package ru.turikhay.tlauncher.ui.scenes;

import java.awt.Dimension;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.InfoPanel;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.SideNotifier;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.Direction;

public class DefaultScene extends PseudoScene {
	// Dimensions:
	public static final Dimension
	LOGIN_SIZE = new Dimension(250, 240),
	SETTINGS_SIZE = new Dimension(500, 475);

	// Insets:
	public static final int
	EDGE_INSETS = 10,
	INSETS = 15; // between login form and side panel

	// Notifier:
	public final SideNotifier notifier;

	// Login form:
	public final LoginForm loginForm;

	// Side panels:
	public final SettingsPanel settingsForm;

	private SidePanel sidePanel; // Current showing side panel
	private ExtendedPanel sidePanelComp;

	// Directions:
	private Direction lfDirection;

	//Additional panels:
	public final InfoPanel infoPanel;

	public DefaultScene(MainPane main) {
		super(main);

		this.notifier = main.notifier;

		this.settingsForm = new SettingsPanel(this);
		settingsForm.setSize(SETTINGS_SIZE);
		settingsForm.setVisible(false);
		add(settingsForm);

		this.loginForm = new LoginForm(this);
		loginForm.setSize(LOGIN_SIZE);
		add(loginForm);

		this.infoPanel = new InfoPanel(this);
		add(infoPanel);

		updateDirection();
	}

	@Override
	public void onResize() {
		if (parent == null)
			return;

		setBounds(0, 0, parent.getWidth(), parent.getHeight());
		updateCoords();
	}

	private void updateCoords() {
		int w = getWidth(), h = getHeight(), hw = w / 2, hh = h / 2;

		int
		lf_w = loginForm.getWidth(),
		lf_h = loginForm.getHeight();
		int lf_x, lf_y;

		if(sidePanel == null) {

			// define x
			switch(lfDirection) {
			case TOP_LEFT: case CENTER_LEFT: case BOTTOM_LEFT:
				lf_x = EDGE_INSETS;
				break;
			case TOP: case CENTER: case BOTTOM:
				lf_x = hw - lf_w / 2;
				break;
			case TOP_RIGHT: case CENTER_RIGHT: case BOTTOM_RIGHT:
				lf_x = w - lf_w - EDGE_INSETS;
				break;
			default:
				throw new RuntimeException("unknown direction:"+ lfDirection);
			}

			// define y
			switch(lfDirection) {
			case TOP_LEFT: case TOP: case TOP_RIGHT:
				lf_y = EDGE_INSETS;
				break;
			case CENTER_LEFT: case CENTER: case CENTER_RIGHT:
				lf_y = hh - lf_h / 2;
				break;
			case BOTTOM_LEFT: case BOTTOM: case BOTTOM_RIGHT:
				lf_y = h - EDGE_INSETS - lf_h;
				break;
			default:
				throw new RuntimeException("unknown direction:"+ lfDirection);
			}
		}
		else
		{
			int
			sp_w = sidePanelComp.getWidth(),
			sp_h = sidePanelComp.getHeight(),
			bw = lf_w + sp_w + INSETS,
			hbw = bw / 2, // Half width of both components
			sp_x, sp_y;

			if(w > bw) {
				// define x
				switch(lfDirection) {
				case TOP_LEFT: case CENTER_LEFT: case BOTTOM_LEFT:
					lf_x = EDGE_INSETS;
					sp_x = lf_x + lf_w + INSETS;
					break;
				case TOP: case CENTER: case BOTTOM:
					lf_x = hw - hbw;
					sp_x = lf_x + INSETS + sp_w / 2;
					break;
				case TOP_RIGHT: case CENTER_RIGHT: case BOTTOM_RIGHT:
					lf_x = w - EDGE_INSETS - lf_w;
					sp_x = lf_x - INSETS - sp_w;
					break;
				default:
					throw new RuntimeException("unknown direction:"+ lfDirection);
				}

				// define y
				switch(lfDirection) {
				case TOP_LEFT: case TOP: case TOP_RIGHT:
					lf_y = sp_y = EDGE_INSETS;
					break;
				case CENTER_LEFT: case CENTER: case CENTER_RIGHT:
					lf_y = hh - lf_h / 2;
					sp_y = hh - sp_h / 2;
					break;
				case BOTTOM_LEFT: case BOTTOM: case BOTTOM_RIGHT:
					lf_y = h - EDGE_INSETS - lf_h;
					sp_y = h - EDGE_INSETS - sp_h;
					break;
				default:
					throw new RuntimeException("unknown direction:"+ lfDirection);
				}
			} else {
				lf_x = w*2;
				lf_y = 0;

				sp_x = hw - sp_w / 2;
				sp_y = hh - sp_h / 2;
			}

			sidePanelComp.setLocation(sp_x, sp_y);
		}

		int n_x, n_y = EDGE_INSETS;

		// define x
		switch(lfDirection) {
		case TOP_LEFT: case CENTER_LEFT: case BOTTOM_LEFT:
			n_x = getMainPane().getWidth() - EDGE_INSETS - notifier.getWidth();
			break;
		default:
			n_x = EDGE_INSETS;
		}

		notifier.setLocation(n_x, n_y);

		loginForm.setLocation(lf_x, lf_y);
		infoPanel.onResize();
	}

	public SidePanel getSidePanel() {
		return sidePanel;
	}

	public void setSidePanel(SidePanel side) {
		if(this.sidePanel == side)
			return;

		boolean noSidePanel = side == null;

		if(this.sidePanelComp != null)
			sidePanelComp.setVisible(false);

		this.sidePanel = side;
		this.sidePanelComp = noSidePanel? null : getSidePanelComp(side);

		if(!noSidePanel)
			sidePanelComp.setVisible(true);

		infoPanel.setShown(noSidePanel, noSidePanel);

		updateCoords();
	}

	public void toggleSidePanel(SidePanel side) {
		if(this.sidePanel == side)
			side = null;
		setSidePanel(side);
	}

	public ExtendedPanel getSidePanelComp(SidePanel side) {
		if(side == null)
			throw new NullPointerException("side");

		switch(side) {
		case SETTINGS:
			return settingsForm;
		default:
			throw new RuntimeException("unknown side:"+ side);
		}
	}

	public Direction getLoginFormDirection() {
		return lfDirection;
	}

	public void updateDirection() {
		loadDirection();
		updateCoords();
	}

	private void loadDirection() {
		Configuration config = getMainPane().getRootFrame().getConfiguration();

		Direction loginFormDirection = config.getDirection("gui.direction.loginform");

		if(loginFormDirection == null)
			loginFormDirection = Direction.CENTER;

		this.lfDirection = loginFormDirection;
	}

	public enum SidePanel {
		SETTINGS;

		public final boolean requiresShow;

		SidePanel(boolean requiresShow) {
			this.requiresShow = requiresShow;
		}

		SidePanel() {
			this(false);
		}
	}
}
