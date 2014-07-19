package ru.turikhay.tlauncher.ui.swing;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.Localizable;

public class AccountCellRenderer implements ListCellRenderer<Account> {
	public static final Account EMPTY = Account.randomAccount(),
			MANAGE = Account.randomAccount();

	private static final Icon MANAGE_ICON = ImageCache.getIcon("gear.png");
	private static final Icon CROWN_ICON = ImageCache.getIcon("crown.png");

	private final DefaultListCellRenderer defaultRenderer;
	private AccountCellType type;

	public AccountCellRenderer(AccountCellType type) {
		if (type == null)
			throw new NullPointerException("CellType cannot be NULL!");

		this.defaultRenderer = new DefaultListCellRenderer();
		this.type = type;
	}

	public AccountCellRenderer() {
		this(AccountCellType.PREVIEW);
	}

	public AccountCellType getType() {
		return type;
	}

	public void setType(AccountCellType type) {
		if (type == null)
			throw new NullPointerException("CellType cannot be NULL!");

		this.type = type;
	}

	@Override
	public Component getListCellRendererComponent(
			JList<? extends Account> list, Account value, int index,
			boolean isSelected, boolean cellHasFocus) {

		JLabel renderer = (JLabel) defaultRenderer
				.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);

		renderer.setAlignmentY(Component.CENTER_ALIGNMENT);

		if (value == null || value.equals(EMPTY))
			renderer.setText(Localizable.get("account.empty"));

		else if (value.equals(MANAGE)) {
			renderer.setText(Localizable.get("account.manage"));
			renderer.setIcon(MANAGE_ICON);
		}

		else {
			switch (type) {
			case EDITOR:
				if (value.isPremium())
					renderer.setIcon(CROWN_ICON);

				if (!value.hasUsername()) {
					renderer.setText(Localizable.get("account.creating"));
					renderer.setFont(renderer.getFont().deriveFont(Font.ITALIC));

					break;
				}
			default:
				renderer.setText(value.getUsername());
			}
		}

		return renderer;
	}

	public enum AccountCellType {
		PREVIEW, EDITOR
	}
}
