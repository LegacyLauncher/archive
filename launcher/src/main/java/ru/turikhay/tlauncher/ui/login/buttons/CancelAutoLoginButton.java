package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;

public class CancelAutoLoginButton extends LocalizableButton {
    private static final long serialVersionUID = 353522972818099436L;

    CancelAutoLoginButton(final LoginForm lf) {
        super("loginform.cancel", lf.autologin.getTimeout());
        addActionListener(e -> {
            lf.autologin.setEnabled(false);
            lf.tlauncher.getVersionManager().asyncRefresh();
        });
    }
}
