package ru.turikhay.tlauncher.ui.login;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.managers.ProfileManagerListener;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.listener.AuthUIListener;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.login.LoginForm.LoginProcessListener;
import ru.turikhay.tlauncher.ui.login.LoginWaitException.LoginWaitTask;
import ru.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import ru.turikhay.tlauncher.ui.swing.SimpleComboBoxModel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class AccountComboBox extends ExtendedComboBox<Account> implements
Blockable, LoginProcessListener, ProfileManagerListener, LocalizableComponent {
	private static final long serialVersionUID = 6618039863712810645L;

	private static final Account
	EMPTY = AccountCellRenderer.EMPTY,
	MANAGE = AccountCellRenderer.MANAGE;

	private final ProfileManager manager;
	private final LoginForm loginForm;

	private final AuthenticatorListener listener;
	private final SimpleComboBoxModel<Account> model;

	private String selectedAccount;

	AccountComboBox(LoginForm lf) {
		super(new AccountCellRenderer());

		this.loginForm = lf;
		this.model = getSimpleModel();

		this.manager = TLauncher.getInstance().getProfileManager();
		manager.addListener(this);

		listener = new AuthUIListener(true, lf);

		this.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Account selected = (Account) getSelectedItem();

				if (selected == null || selected.equals(EMPTY))
					return;

				if(selected.equals(MANAGE)) {
					loginForm.pane.openAccountEditor();

					setSelectedIndex(0);
					return;
				}

				selectedAccount = selected.getUsername();
			}
		});
		this.selectedAccount = lf.global.get("login.account");
	}

	public Account getAccount() {
		Account value = (Account) getSelectedItem();
		return (value == null || value.equals(EMPTY)) ? null : value;
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
	public void logginingIn() throws LoginException {
		final Account account = getAccount();

		if (account == null) {
			loginForm.pane.openAccountEditor();
			Alert.showLocError("account.empty.error");
			throw new LoginException("Account list is empty!");
		}

		if (account.isPremium())
			throw new LoginWaitException("Waiting for auth...",
					new LoginWaitTask() {
				@Override
				public void runTask() {
					account.getAuthenticator().pass(listener);
				}
			});
	}

	@Override
	public void loginFailed() {
	}

	@Override
	public void loginSucceed() {
	}

	public void refreshAccounts(AuthenticatorDatabase db, String select) {
		if (select == null && selectedAccount != null)
			select = selectedAccount;

		removeAllItems();

		Collection<Account> list = db.getAccounts();

		if (list.isEmpty())
			addItem(EMPTY);
		else {
			model.addElements(list);

			for(Account account : list)
				if(select != null && select.equals(account.getUsername()))
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
