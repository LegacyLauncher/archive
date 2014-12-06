package ru.turikhay.tlauncher.ui.login;

public class LoginWaitException extends LoginException {
	private final LoginWaitTask waitTask;

	public LoginWaitException(String reason, LoginWaitTask waitTask) {
		super(reason);

		if (waitTask == null)
			throw new NullPointerException("wait task");

		this.waitTask = waitTask;
	}

	public LoginWaitTask getWaitTask() {
		return waitTask;
	}

	public interface LoginWaitTask {
		public void runTask() throws LoginException;
	}
}
