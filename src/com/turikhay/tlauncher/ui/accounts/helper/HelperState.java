package com.turikhay.tlauncher.ui.accounts.helper;

import com.turikhay.tlauncher.ui.loc.LocalizableMenuItem;

public enum HelperState {
	LICENSE, PIRATE, HELP(false), NONE;

	public final LocalizableMenuItem item;
	public final boolean showInList;

	HelperState() {
		this(true);
	}

	HelperState(boolean showInList) {
		this.item = new LocalizableMenuItem("auth.helper." + toString());
		this.showInList = showInList;
	}

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}
}
