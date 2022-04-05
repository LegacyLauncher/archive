package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CancelAutoLoginButton extends LocalizableButton {
    private static final long serialVersionUID = 353522972818099436L;

    CancelAutoLoginButton(final LoginForm lf) {
        super("loginform.cancel", Integer.valueOf(lf.autologin.getTimeout()));
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lf.autologin.setEnabled(false);
                lf.tlauncher.getVersionManager().asyncRefresh();
            }
        });
    }
}
