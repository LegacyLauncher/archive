package ru.turikhay.tlauncher.ui.login;

public class LoginWaitException extends LoginException {
    private final LoginWaitException.LoginWaitTask waitTask;

    public LoginWaitException(String reason, LoginWaitException.LoginWaitTask waitTask) {
        super(reason);
        if (waitTask == null) {
            throw new NullPointerException("wait task");
        } else {
            this.waitTask = waitTask;
        }
    }

    public LoginWaitException.LoginWaitTask getWaitTask() {
        return waitTask;
    }

    public interface LoginWaitTask {
        void runTask() throws LoginException;
    }
}
