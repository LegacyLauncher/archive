package ru.turikhay.tlauncher.ui.accounts;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.accounts.UsernameField.UsernameState;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.CheckBoxListener;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.text.ExtendedPasswordField;

public class AccountEditor extends CenterPanel {
	private static final long serialVersionUID = 7061277150214976212L;

	private final AccountEditorScene scene;

	public final UsernameField username;
	public final ExtendedPasswordField password;

	public final LocalizableCheckbox premiumBox;
	public final LocalizableButton save;

	private final ProgressBar progressBar;

	public AccountEditor(AccountEditorScene sc) {
		super(squareInsets);

		this.scene = sc;

		this.username = new UsernameField(this, UsernameState.USERNAME);

		this.password = new ExtendedPasswordField();
		password.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				defocus();
				scene.handler.saveEditor();
			}
		});
		password.setEnabled(false);

		premiumBox = new LocalizableCheckbox("account.premium");
		premiumBox.addItemListener(new CheckBoxListener() {
			@Override
			public void itemStateChanged(boolean newstate) {
				if (newstate && !password.hasPassword())
					password.setText(null);

				password.setEnabled(newstate);
				username.setState(newstate ? UsernameState.EMAIL
						: UsernameState.USERNAME);

				defocus();
			}
		});

		save = new LocalizableButton("account.save");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				defocus();
				scene.handler.saveEditor();
			}
		});

		progressBar = new ProgressBar();
		progressBar.setPreferredSize(new Dimension(200, 20));

		this.add(del(Del.CENTER));
		this.add(sepPan(username));
		this.add(sepPan(premiumBox));
		this.add(sepPan(password));
		this.add(del(Del.CENTER));
		this.add(sepPan(save));
		this.add(sepPan(progressBar));
	}

	public void fill(Account account) {
		this.premiumBox.setSelected(account.isPremium());
		this.username.setText(account.getUsername());
		this.password.setText(null);
	}

	public void clear() {
		this.premiumBox.setSelected(false);
		this.username.setText(null);
		this.password.setText(null);
	}

	public Account get() {
		Account account = new Account();
		account.setUsername(username.getValue());

		if (premiumBox.isSelected()) {
			account.setPremium(true);

			if (password.hasPassword())
				account.setPassword(password.getPassword());
		}

		return account;
	}

	@Override
	public Insets getInsets() {
		return squareInsets;
	}

	@Override
	public void block(Object reason) {
		super.block(reason);

		password.setEnabled(premiumBox.isSelected());

		if (!reason.equals("empty"))
			progressBar.setIndeterminate(true);
	}

	@Override
	public void unblock(Object reason) {
		super.unblock(reason);

		password.setEnabled(premiumBox.isSelected());

		if (!reason.equals("empty"))
			progressBar.setIndeterminate(false);
	}
}
