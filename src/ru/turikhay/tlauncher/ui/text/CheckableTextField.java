package ru.turikhay.tlauncher.ui.text;

import ru.turikhay.tlauncher.ui.center.CenterPanel;

/**
 * <code>CheckableTextField</code> is an abstract implementation of
 * <code>ExtendedTextField</code> that adds ability to check the validity of
 * <code>JTextField</code> values.
 * 
 * @author Artur Khusainov
 * @see ExtendedTextField
 * @see #check()
 * 
 */
public abstract class CheckableTextField extends ExtendedTextField {
	private static final long serialVersionUID = 2835507963141686372L;

	private CenterPanel parent;

	protected CheckableTextField(CenterPanel panel, String placeholder,
			String value) {
		super(panel, placeholder, value);
		this.parent = panel;
	}

	public CheckableTextField(String placeholder, String value) {
		this(null, placeholder, value);
	}

	public CheckableTextField(String placeholder) {
		this(null, placeholder, null);
	}

	public CheckableTextField(CenterPanel panel) {
		this(panel, null, null);
	}

	boolean check() {
		String text = getValue(), result = check(text);

		if (result == null)
			return setValid();
		return setInvalid(result);
	}

	public boolean setInvalid(String reason) {
		this.setBackground(getTheme().getFailure());
		this.setForeground(getTheme().getFocus());

		if (parent != null)
			parent.setError(reason);

		return false;
	}

	public boolean setValid() {
		this.setBackground(getTheme().getBackground());
		this.setForeground(getTheme().getFocus());

		if (parent != null)
			parent.setError(null);

		return true;
	}

	@Override
	protected void updateStyle() {
		super.updateStyle();
		check();
	}

	@Override
	protected void onChange() {
		check();
	}

	protected abstract String check(String text);
}
