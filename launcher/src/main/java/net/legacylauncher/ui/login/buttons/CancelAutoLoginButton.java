package net.legacylauncher.ui.login.buttons;

import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.login.LoginForm;

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
