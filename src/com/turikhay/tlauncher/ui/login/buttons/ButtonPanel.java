package com.turikhay.tlauncher.ui.login.buttons;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.tlauncher.ui.login.LoginForm;

public class ButtonPanel extends BlockablePanel {
	private static final long serialVersionUID = -2155145867054136409L;

	public final PlayButton play;

	private final JPanel manageButtonsPanel;
	public final SupportButton support;
	public final FolderButton folder;
	public final RefreshButton refresh;
	public final SettingsButton settings;

	public final CancelAutoLoginButton cancel;

	private ButtonPanelState state;

	public ButtonPanel(LoginForm lf) {
		BorderLayout lm = new BorderLayout(1, 2);
		this.setLayout(lm);
		this.setOpaque(false);

		play = new PlayButton(lf);
		this.add("Center", play);

		cancel = new CancelAutoLoginButton(lf);

		this.manageButtonsPanel = new JPanel(new GridLayout(0, 4));
		manageButtonsPanel.setOpaque(false);

		support = new SupportButton(lf);
		manageButtonsPanel.add(support);

		folder = new FolderButton(lf);
		manageButtonsPanel.add(folder);

		refresh = new RefreshButton(lf);
		manageButtonsPanel.add(refresh);

		settings = new SettingsButton(lf);
		manageButtonsPanel.add(settings);

		setState(lf.autologin.isEnabled() ? ButtonPanelState.AUTOLOGIN_CANCEL
				: ButtonPanelState.MANAGE_BUTTONS);
	}

	public ButtonPanelState getState() {
		return state;
	}

	public void setState(ButtonPanelState state) {
		if (state == null)
			throw new NullPointerException();

		this.state = state;

		switch (state) {
		case AUTOLOGIN_CANCEL:
			remove(manageButtonsPanel);
			add("South", cancel);
			break;
		case MANAGE_BUTTONS:
			remove(cancel);
			add("South", manageButtonsPanel);
			break;
		default:
			throw new IllegalArgumentException("Unknown state: " + state);
		}

		validate();
	}

	public enum ButtonPanelState {
		AUTOLOGIN_CANCEL, MANAGE_BUTTONS
	}
}
