package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.login.LoginForm.LoginState;
import ru.turikhay.tlauncher.ui.login.LoginForm.LoginStateListener;

public class PlayButton extends LocalizableButton implements Blockable, LoginStateListener {
	private static final long serialVersionUID = 6944074583143406549L;

	private PlayButtonState state;

	private final LoginForm loginForm;

	PlayButton(LoginForm lf) {
		this.loginForm = lf;

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(state) {
				case CANCEL:
					loginForm.stopLauncher();
					break;
				default:
					loginForm.startLauncher();
					break;

				}
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

		if(state == PlayButtonState.CANCEL)
			setEnabled(true);
	}

	public void updateState() {
		VersionSyncInfo vs = loginForm.versions.getVersion();

		if (vs == null)
			return;

		boolean
		installed = vs.isInstalled(),
		force = loginForm.checkbox.forceupdate.getState();

		if (!installed)
			setState(PlayButtonState.INSTALL);
		else
			setState(force ? PlayButtonState.REINSTALL : PlayButtonState.PLAY);
	}

	public enum PlayButtonState {
		REINSTALL("loginform.enter.reinstall"),
		INSTALL("loginform.enter.install"),
		PLAY("loginform.enter"),
		CANCEL("loginform.enter.cancel");

		private final String path;

		PlayButtonState(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

	@Override
	public void loginStateChanged(LoginState state) {
		if(state == LoginState.LAUNCHING) {
			setState(PlayButtonState.CANCEL);
		} else {
			updateState();
			setEnabled(!Blocker.isBlocked(this));
		}
	}

	@Override
	public void block(Object reason) {
		if(state != PlayButtonState.CANCEL)
			setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		setEnabled(true);
	}
}
