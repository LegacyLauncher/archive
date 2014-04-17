package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.swing.extended.ExtendedButton;

public class LocalizableButton extends ExtendedButton implements
		LocalizableComponent {
	private static final long serialVersionUID = 1073130908385613323L;

	private String path;
	private String[] variables;

	protected LocalizableButton() {
	}

	public LocalizableButton(String path) {
		this();
		setText(path);
	}

	public LocalizableButton(String path, Object... vars) {
		this();
		setText(path, vars);
	}

	public void setText(String path, Object... vars) {
		this.path = path;
		this.variables = Localizable.checkVariables(vars);

		String value = Localizable.get(path);
		for (int i = 0; i < variables.length; i++)
			value = value.replace("%" + i, variables[i]);

		super.setText(value);
	}

	@Override
	public void setText(String path) {
		setText(path, Localizable.EMPTY_VARS);
	}

	@Override
	public void updateLocale() {
		setText(path, (Object[]) variables);
	}
}
