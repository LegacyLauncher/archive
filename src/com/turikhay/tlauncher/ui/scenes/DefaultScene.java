package com.turikhay.tlauncher.ui.scenes;

import java.awt.Component;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.animate.Animator;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.info.InfoPanel;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.settings.SettingsPanel;
import com.turikhay.tlauncher.ui.swing.ResizeableComponent;

public class DefaultScene extends PseudoScene {
	private static final long serialVersionUID = -1460877989848190921L;

	private final int LOGINFORM_WIDTH;
	private final int LOGINFORM_HEIGHT;
	private final int SETTINGSFORM_WIDTH;
	private final int SETTINGSFORM_HEIGHT;
	private final int MARGIN;

	public final LoginForm loginForm;
	public final SettingsPanel settingsForm;

	private final InfoPanel infoPanel;

	private boolean settings;

	public DefaultScene(MainPane main) {
		super(main);

		LOGINFORM_WIDTH = 250;
		LOGINFORM_HEIGHT = 240;

		SETTINGSFORM_WIDTH = 500;
		SETTINGSFORM_HEIGHT = 480;

		MARGIN = 25; // between loginForm and settingsForm

		this.settingsForm = new SettingsPanel(this);
		settingsForm.setSize(SETTINGSFORM_WIDTH, SETTINGSFORM_HEIGHT);
		add(settingsForm);

		this.loginForm = new LoginForm(this);
		loginForm.setSize(LOGINFORM_WIDTH, LOGINFORM_HEIGHT);
		add(loginForm);

		this.infoPanel = new InfoPanel(this);
		infoPanel.setSize(200, 35);
		add(infoPanel);

		setSettings(false, false);
	}

	@Override
	public void onResize() {
		if (parent == null)
			return;

		setBounds(0, 0, parent.getWidth(), parent.getHeight());

		setSettings(settings, false);
	}

	void setSettings(boolean shown, boolean update) {
		if (settings == shown && update)
			return;

		if (shown)
			settingsForm.unblock("");
		else
			settingsForm.block("");

		int w = getWidth(), h = getHeight(), hw = w / 2, hh = h / 2;
		int lf_x, lf_y, sf_x, sf_y;

		if (shown) {
			int bw = LOGINFORM_WIDTH + SETTINGSFORM_WIDTH + MARGIN, hbw = bw / 2; // bw
																					// =
																					// width
																					// of
																					// lf
																					// and
																					// sf.

			lf_x = hw - hbw;
			lf_y = hh - LOGINFORM_HEIGHT / 2;
			sf_x = hw - hbw + SETTINGSFORM_WIDTH / 2 + MARGIN;
			sf_y = hh - SETTINGSFORM_HEIGHT / 2;
		} else {
			lf_x = hw - LOGINFORM_WIDTH / 2;
			lf_y = hh - LOGINFORM_HEIGHT / 2;
			sf_x = w * 2;
			sf_y = hh - SETTINGSFORM_HEIGHT / 2;
		}

		Animator.move(loginForm, lf_x, lf_y);
		Animator.move(settingsForm, sf_x, sf_y);

		infoPanel.setShown(!shown, false);

		for (Component comp : getComponents())
			if (comp instanceof ResizeableComponent)
				((ResizeableComponent) comp).onResize();

		settings = shown;
	}

	public void setSettings(boolean shown) {
		setSettings(shown, true);
	}

	public void toggleSettings() {
		this.setSettings(!settings);
	}

	public boolean isSettingsShown() {
		return settings;
	}

	@Override
	public void block(Object reason) {
		Blocker.block(reason, loginForm, settingsForm);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblock(reason, loginForm, settingsForm);
	}

}
