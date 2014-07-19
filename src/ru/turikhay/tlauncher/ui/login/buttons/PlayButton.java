package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;

import net.minecraft.launcher.updater.VersionSyncInfo;

public class PlayButton extends LocalizableButton {
	private static final long serialVersionUID = 6944074583143406549L;
	
	private PlayButtonState state;

	private final LoginForm loginForm;

	PlayButton(LoginForm lf) {
		this.loginForm = lf;

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loginForm.callLogin();
			}
		});
		this.setFont(getFont().deriveFont(Font.BOLD).deriveFont(16.0F));
		this.setState(PlayButtonState.PLAY);
	}

	public PlayButtonState getState() {
		return state;
	}

	public void setState(PlayButtonState state) {
		if (state == null)
			throw new NullPointerException();

		this.state = state;
		this.setText(state.getPath());
	}

	public void updateState() {
		VersionSyncInfo vs = loginForm.versions.getVersion();

		if (vs == null)
			return;

		boolean installed = vs.isInstalled(), force = loginForm.checkbox.forceupdate
				.getState();

		if (!installed)
			setState(PlayButtonState.INSTALL);
		else
			setState(force ? PlayButtonState.REINSTALL : PlayButtonState.PLAY);
	}

	public enum PlayButtonState {
		REINSTALL("loginform.enter.reinstall"),
		INSTALL("loginform.enter.install"),
		PLAY("loginform.enter");

		private final String path;

		PlayButtonState(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}
}
