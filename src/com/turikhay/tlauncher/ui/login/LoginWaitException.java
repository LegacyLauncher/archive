package com.turikhay.tlauncher.ui.login;

public class LoginWaitException extends LoginException {
	private static final long serialVersionUID = -6606669907221918306L;

	private final LoginWaitTask waitTask;

	public LoginWaitException(String reason, final LoginWaitTask loginWaitTask) {
		super(reason);

		if (loginWaitTask == null)
			throw new NullPointerException();

		this.waitTask = loginWaitTask;
	}

	public LoginWaitTask getWaitTask() {
		return waitTask;
	}

	public interface LoginWaitTask {
		public void runTask() throws LoginException;
	}
}
