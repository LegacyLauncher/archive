package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class AccountConverter extends LocalizableStringConverter<Account> {
	private final ProfileManager pm;

	public AccountConverter(ProfileManager pm) {
		super(null);

		if (pm == null)
			throw new NullPointerException();

		this.pm = pm;
	}

	@Override
	public String toString(Account from) {
		if (from == null)
			return Localizable.get("account.empty");
		if (from.getUsername() == null)
			return null;

		return from.getUsername();
	}

	@Override
	public Account fromString(String from) {
		return pm.getAuthDatabase().getByUsername(from);
	}

	@Override
	public String toValue(Account from) {
		if (from == null || from.getUsername() == null)
			return null;
		return from.getUsername();
	}

	@Override
	public String toPath(Account from) {
		return null;
	}
}
