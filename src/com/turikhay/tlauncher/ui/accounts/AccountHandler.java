package com.turikhay.tlauncher.ui.accounts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPopupMenu;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.accounts.helper.HelperState;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.listener.AuthUIListener;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.util.U;

public class AccountHandler {
	private final AccountEditorScene scene;

	public final AccountList list;
	public final AccountEditor editor;

	private final ProfileManager manager;
	private final AuthUIListener listener;

	private Account lastAccount;
	private Account tempAccount;

	private JPopupMenu popup;

	public AccountHandler(AccountEditorScene sc) {
		this.manager = TLauncher.getInstance().getProfileManager();
		this.scene = sc;

		this.list = scene.list;
		this.editor = scene.editor;

		this.popup = new JPopupMenu();

		for (final HelperState state : HelperState.values()) {
			if (!state.showInList)
				continue;

			state.item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					scene.helper.setState(state);
				}
			});

			popup.add(state.item);
		}

		this.listener = new AuthUIListener(false, new AuthenticatorListener() {
			@Override
			public void onAuthPassing(Authenticator auth) {
				block();
			}

			@Override
			public void onAuthPassingError(Authenticator auth, Throwable e) {
				unblock();
			}

			@Override
			public void onAuthPassed(Authenticator auth) {
				unblock();
				registerTemp();
			}

		});
	}

	public void selectAccount(Account acc) {
		if (acc == null)
			return;
		if (acc.equals(list.list.getSelectedValue()))
			return;

		list.list.setSelectedValue(acc, true);
	}

	void refreshEditor(Account account) {
		if (account == null) {
			clearEditor();
			return;
		}

		if (account.equals(lastAccount))
			return;

		lastAccount = account;

		Blocker.unblock(editor, "empty");
		editor.fill(account);

		if (!account.equals(tempAccount))
			scene.getMainPane().defaultScene.loginForm.accounts
					.setAccount(lastAccount);
	}

	void clearEditor() {
		lastAccount = null;
		editor.clear();

		if (!list.model.isEmpty())
			list.list.setSelectedValue(lastAccount, true);
		else
			notifyEmpty();
	}

	void saveEditor() {
		if (lastAccount == null)
			return;

		Account acc = editor.get();

		if (acc.getUsername() == null) {
			Alert.showLocError("auth.error.nousername");
			return;
		}

		lastAccount.complete(acc);

		if (lastAccount.hasLicense()) {
			if (lastAccount.getAccessToken() == null
					&& lastAccount.getPassword() == null) {
				Alert.showLocError("auth.error.nopass");
				return;
			}
			lastAccount.getAuthenticator().asyncPass(listener);
		} else {
			registerTemp();
			listener.saveProfiles();
		}
	}

	void exitEditor() {
		scene.getMainPane().openDefaultScene();
		listener.saveProfiles();
	}

	void addAccount() {
		if (tempAccount != null)
			return;

		this.tempAccount = new Account();

		list.model.addElement(tempAccount);
		list.list.setSelectedValue(tempAccount, true);
		refreshEditor(tempAccount);
	}

	void removeAccount() {
		if (lastAccount == null)
			return;

		Account acc = lastAccount;

		list.model.removeElement(lastAccount);

		this.lastAccount = acc;

		if (tempAccount != null) {
			tempAccount = null;
			clearEditor();
			return;
		}

		U.log("Removing", lastAccount);
		manager.getAuthDatabase().unregisterAccount(lastAccount);

		clearEditor();
		listener.saveProfiles();
	}

	void registerTemp() {
		if (tempAccount == null)
			return;

		manager.getAuthDatabase().registerAccount(tempAccount);
		scene.getMainPane().defaultScene.loginForm.accounts.refreshAccounts(
				manager.getAuthDatabase(), tempAccount.getUsername());

		tempAccount = null;
	}

	void notifyEmpty() {
		Blocker.block(editor, "empty");

		if (scene.helper.isShowing())
			scene.helper.setState(HelperState.HELP);
	}

	void callPopup() {
		if (popup.isShowing())
			return;
		popup.show(list.help, 0, list.help.getHeight());
	}

	private void block() {
		Blocker.block("auth", editor, list);
	}

	private void unblock() {
		Blocker.unblock("auth", editor, list);
	}
}
