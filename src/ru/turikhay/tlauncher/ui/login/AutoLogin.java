package ru.turikhay.tlauncher.ui.login;

import ru.turikhay.tlauncher.ui.login.LoginForm.LoginProcessListener;
import ru.turikhay.tlauncher.ui.login.buttons.ButtonPanel.ButtonPanelState;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class AutoLogin implements LoginProcessListener {
	public final static int DEFAULT_TIMEOUT = 3, MIN_TIMEOUT = 2, MAX_TIMEOUT = 10;

	private boolean enabled, active;
	private int timeout, sec;
	private Runnable task;

	private final LoginForm loginForm;

	AutoLogin(LoginForm lf) {
		this.loginForm = lf;
		this.enabled = lf.global.getBoolean("login.auto");

		int timeout = lf.global.getInteger("login.auto.timeout");
		if (timeout < MIN_TIMEOUT || timeout > MAX_TIMEOUT)
			timeout = DEFAULT_TIMEOUT;

		this.timeout = timeout;

		this.task = new Runnable() {
			@Override
			public void run() {
				while (sec > 0) {
					U.sleepFor(1000);

					if (updateLogin())
						loginForm.startLauncher();
				}
			}
		};
	}

	private boolean updateLogin() {
		--sec;

		loginForm.buttons.cancel.setText("loginform.cancel", sec);

		if (sec != 0)
			return false;

		stopActive();
		return true;
	}

	public void setActive(boolean active) {
		if (this.active == active)
			return;

		this.active = active;

		if (active)
			startActive();
		else
			stopActive();
	}

	public boolean isActive() {
		return active;
	}

	private void startActive() {
		this.sec = timeout;
		AsyncThread.execute(task);
	}

	private void stopActive() {
		sec = -1;
		loginForm.buttons.setState(ButtonPanelState.MANAGE_BUTTONS);
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled)
			return;

		this.enabled = enabled;
		if (active)
			setActive(enabled);

		loginForm.checkbox.autologin.setSelected(enabled);
		loginForm.global.set("login.auto", enabled);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getTimeout() {
		return timeout;
	}

	@Override
	public void logginingIn() throws LoginException {
		setActive(false);
	}

	@Override
	public void loginFailed() {
	}

	@Override
	public void loginSucceed() {
	}

}
