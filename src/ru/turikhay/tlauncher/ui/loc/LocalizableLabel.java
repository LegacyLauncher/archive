package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;

public class LocalizableLabel extends ExtendedLabel implements
LocalizableComponent {
	private static final long serialVersionUID = 7628068160047735335L;

	protected String path;
	protected String[] variables;

	public LocalizableLabel(String path, Object... vars) {
		init();

		setText(path, vars);
		setFont(getFont().deriveFont(TLauncherFrame.fontSize));
	}

	public LocalizableLabel(String path) {
		this(path, Localizable.EMPTY_VARS);
	}

	public LocalizableLabel() {
		this(null);
	}

	public LocalizableLabel(int horizontalAlignment) {
		this(null);
		this.setHorizontalAlignment(horizontalAlignment);
	}

	public void setText(String path, Object... vars) {
		this.path = path;
		this.variables = Localizable.checkVariables(vars);

		String value = Localizable.get(path);
		for (int i = 0; i < variables.length; i++)
			value = value.replace("%" + i, variables[i]);

		setRawText(value);
	}

	protected void setRawText(String value) {
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

	protected void init() {

	}
}
