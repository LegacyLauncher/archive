package com.turikhay.tlauncher.ui.accounts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.managers.ProfileManagerListener;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.swing.AccountCellRenderer;
import com.turikhay.tlauncher.ui.swing.AccountCellRenderer.AccountCellType;
import com.turikhay.tlauncher.ui.swing.ImageButton;

public class AccountList extends CenterPanel {
	private static final long serialVersionUID = 3280495266368287215L;

	private final AccountEditorScene scene;

	public final DefaultListModel<Account> model;
	public final JList<Account> list;

	public final ImageButton add;
	private final ImageButton remove;
	public final ImageButton help;
	public final ImageButton back;

	public AccountList(AccountEditorScene sc) {
		super(squareInsets);

		this.scene = sc;

		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setOpaque(false);

		LocalizableLabel label = new LocalizableLabel("account.list");
		panel.add("North", label);

		this.model = new DefaultListModel<Account>();
		this.list = new JList<Account>(model);
		list.setCellRenderer(new AccountCellRenderer(AccountCellType.EDITOR));
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Account account = list.getSelectedValue();
				scene.handler.refreshEditor(account);
			}
		});

		JScrollPane scroll = new JScrollPane(list);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setBorder(null);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		panel.add("Center", scroll);

		JPanel buttons = new JPanel(new GridLayout(0, 4));
		buttons.setOpaque(false);

		this.add = new ImageButton("add.png");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scene.handler.addAccount();
				defocus();
			}
		});
		buttons.add(add);

		this.remove = new ImageButton("remove.png");
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scene.handler.removeAccount();
				defocus();
			}
		});
		buttons.add(remove);

		this.help = new ImageButton("info.png");
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				defocus();
				scene.handler.callPopup();
			}
		});
		buttons.add(help);

		this.back = new ImageButton("home.png");
		back.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scene.handler.exitEditor();
			}
		});
		buttons.add(back);

		panel.add("South", buttons);

		this.add(panel);

		ProfileManagerListener listener = new ProfileManagerListener() {
			@Override
			public void onProfilesRefreshed(ProfileManager pm) {
				refreshFrom(pm.getAuthDatabase());
			}

			@Override
			public void onProfileManagerChanged(ProfileManager pm) {
				refreshFrom(pm.getAuthDatabase());
			}

			@Override
			public void onAccountsRefreshed(AuthenticatorDatabase db) {
				refreshFrom(db);
			}
		};
		TLauncher.getInstance().getProfileManager().addListener(listener);
	}

	void refreshFrom(AuthenticatorDatabase db) {
		model.removeAllElements();

		for (Account account : db.getAccounts())
			model.addElement(account);

		if (model.isEmpty())
			scene.handler.notifyEmpty();
	}

}
