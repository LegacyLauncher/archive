package ru.turikhay.tlauncher.ui.loc;

public class LocalizableHTMLLabel extends LocalizableLabel {
	public LocalizableHTMLLabel(String path, Object... vars) {
		super(path, vars);
	}

	public LocalizableHTMLLabel(String path) {
		this(path, Localizable.EMPTY_VARS);
	}

	public LocalizableHTMLLabel() {
		this(null);
	}

	public LocalizableHTMLLabel(int horizontalAlignment) {
		this(null);
		setHorizontalAlignment(horizontalAlignment);
	}

	@Override
	public void setText(String path, Object... vars) {
		this.path = path;
		this.variables = Localizable.checkVariables(vars);

		String value = Localizable.get(path);

		if(value != null) {
			value = "<html>" + value.replace("\n", "<br/>") + "</html>";

			for (int i = 0; i < variables.length; i++)
				value = value.replace("%" + i, variables[i]);
		}

		setRawText(value);
	}

}
