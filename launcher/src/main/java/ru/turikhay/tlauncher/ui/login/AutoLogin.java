package ru.turikhay.tlauncher.ui.login;

import ru.turikhay.tlauncher.ui.login.buttons.ButtonPanel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class AutoLogin implements LoginForm.LoginProcessListener {
    public static final int DEFAULT_TIMEOUT = 3;
    public static final int MIN_TIMEOUT = 2;
    public static final int MAX_TIMEOUT = 10;
    private boolean enabled;
    private boolean active;
    private final int timeout;
    private int sec;
    private final Runnable task;
    private final LoginForm loginForm;

    AutoLogin(LoginForm lf) {
        loginForm = lf;
        enabled = lf.global.getBoolean("login.auto");
        int timeout = lf.global.getInteger("login.auto.timeout");
        if (timeout < 2 || timeout > 10) {
            timeout = 3;
        }

        this.timeout = timeout;
        task = () -> {
            while (sec > 0) {
                U.sleepFor(1000L);
                if (updateLogin()) {
                    loginForm.startLauncher();
                }
            }

        };
    }

    private boolean updateLogin() {
        --sec;
        loginForm.buttons.cancel.setText("loginform.cancel", sec);
        if (sec != 0) {
            return false;
        } else {
            stopActive();
            return true;
        }
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            if (active) {
                startActive();
            } else {
                stopActive();
            }

        }
    }

    public boolean isActive() {
        return active;
    }

    private void startActive() {
        sec = timeout;
        AsyncThread.execute(task);
    }

    private void stopActive() {
        sec = -1;
        loginForm.buttons.setState(ButtonPanel.ButtonPanelState.MANAGE_BUTTONS);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (active) {
                setActive(enabled);
            }

            if (loginForm.checkbox.autologin.isSelected() != enabled) {
                loginForm.checkbox.autologin.setSelected(enabled);
            }

            loginForm.global.set("login.auto", enabled);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTimeout() {
        return timeout;
    }

    public void logginingIn() throws LoginException {
        setActive(false);
    }

    public void loginFailed() {
    }

    public void loginSucceed() {
    }
}
