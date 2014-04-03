package com.turikhay.tlauncher.ui.login;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.managers.ProfileManagerListener;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.listener.AuthUIListener;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.login.LoginWaitException.LoginWaitTask;
import com.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class AccountComboBox extends ExtendedComboBox<Account> implements
		Blockable, LoginListener, ProfileManagerListener, LocalizableComponent {
	private static final long serialVersionUID = 6618039863712810645L;

	private static final Account EMPTY = AccountCellRenderer.EMPTY;
	private static final Account MANAGE = AccountCellRenderer.MANAGE;

	private final ProfileManager manager;
	private final LoginForm loginForm;

	private final AuthenticatorListener listener;

	private String selectedAccount;

	AccountComboBox(LoginForm lf) {
		super(new AccountCellRenderer());

		this.loginForm = lf;

		this.manager = TLauncher.getInstance().getProfileManager();
		manager.addListener(this);

		listener = new AuthUIListener(true, lf);

		this.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Account selected = (Account) getSelectedItem();
				if (selected == null)
					return;
				else if (selected.equals(EMPTY))
					return;
				else if (selected.equals(MANAGE)) {
					loginForm.pane.openAccountEditor();
					setAccount(selectedAccount);

					return;
				}

				selectedAccount = selected.getUsername();
			}
		});
		this.selectedAccount = lf.global.get("login.account");
	}

	public Account getAccount() {
		Account value = (Account) getSelectedItem();
		return (value == null || value.equals(EMPTY) || value.equals(MANAGE)) ? null
				: value;
	}

	public void setAccount(Account account) {
		if (account == null)
			return;
		if (account.equals(getAccount()))
			return;

		this.setSelectedItem(account);
	}

	void setAccount(String username) {
		if (username != null)
			this.setSelectedItem(manager.getAuthDatabase().getByUsername(
					username));
	}

	@Override
	public void onLogin() throws LoginException {
		final Account account = getAccount();

		if (account == null) {
			loginForm.pane.openAccountEditor();
			throw new LoginException("Account list is empty!");
		}

		if (account.hasLicense())
			throw new LoginWaitException("Waiting for auth...",
					new LoginWaitTask() {
						@Override
						public void runTask() {
							account.getAuthenticator().pass(listener);
						}
					});
	}

	@Override
	public void onLoginFailed() {
	}

	@Override
	public void onLoginSuccess() {
	}

	public void refreshAccounts(AuthenticatorDatabase db, String select) {
		if (select == null && selectedAccount != null)
			select = selectedAccount;

		removeAllItems();

		Collection<Account> list = db.getAccounts();

		if (list.isEmpty())
			addItem(EMPTY);
		else
			for (Account account : list) {
				addItem(account);

				if (select != null && select.equals(account.getUsername()))
					setSelectedItem(account);
			}

		addItem(MANAGE);
	}

	@Override
	public void updateLocale() {
		refreshAccounts(manager.getAuthDatabase(), null);
	}

	@Override
	public void onAccountsRefreshed(AuthenticatorDatabase db) {
		refreshAccounts(db, null);
	}

	@Override
	public void onProfilesRefreshed(ProfileManager pm) {
		refreshAccounts(pm.getAuthDatabase(), null);
	}

	@Override
	public void onProfileManagerChanged(ProfileManager pm) {
		refreshAccounts(pm.getAuthDatabase(), null);
	}

	@Override
	public void block(Object reason) {
		this.setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		this.setEnabled(true);
	}

}
