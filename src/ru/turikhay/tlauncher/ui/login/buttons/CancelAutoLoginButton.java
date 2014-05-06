package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.login.LoginForm;

public class CancelAutoLoginButton extends LocalizableButton {
	private static final long serialVersionUID = 353522972818099436L;

	CancelAutoLoginButton(final LoginForm lf) {
		super("loginform.cancel", lf.autologin.getTimeout());

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lf.autologin.setEnabled(false);
				lf.tlauncher.getVersionManager().asyncRefresh();
			}
		});
	}

}
