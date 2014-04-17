package com.turikhay.tlauncher.ui.text;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.center.CenterPanelTheme;

/**
 * <code>ExtendedTextField</code> adds into <code>JTextField</code> ability to
 * set placeholders and to get pointful values.
 * 
 * @author Artur Khusainov
 * @see #getValue()
 * 
 */
public class ExtendedTextField extends JTextField {
	private static final long serialVersionUID = -1963422246993419362L;

	private CenterPanelTheme theme;
	private String placeholder, oldPlaceholder;

	protected ExtendedTextField(CenterPanel panel, String placeholder,
			String value) {
		this.theme = (panel == null) ? CenterPanel.defaultTheme : panel
				.getTheme();
		this.placeholder = placeholder;

		this.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				onFocusGained();
			}

			@Override
			public void focusLost(FocusEvent e) {
				onFocusLost();
			}
		});

		this.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}
		});

		this.setValue(value);
	}

	public ExtendedTextField(String placeholder, String value) {
		this(null, placeholder, value);
	}

	public ExtendedTextField(String placeholder) {
		this(null, placeholder, null);
	}

	/**
	 * This method shouldn't be used to get an pointful value of current
	 * <code>JTextField</code> because it may return placeholder or empty
	 * string.
	 * 
	 * @deprecated use <code>getValue()</code> method instead.
	 * @return <i>NOT</i> pointful value of current <code>TextField</code>
	 */
	@Override
	@Deprecated
	public String getText() {
		return super.getText();
	}

	/**
	 * This method is called to get an pointful value of any <code>String</code>
	 * for current <code>JTextField</code>
	 * 
	 * @param value
	 *            input string
	 * @return pointful value of <value>String</code>
	 */
	private String getValueOf(String value) {
		if (value == null || value.isEmpty() || value.equals(placeholder)
				|| value.equals(oldPlaceholder))
			return null;

		return value;
	}

	/**
	 * This method is called to get an pointful value of the
	 * <code>JTextField</code>
	 * 
	 * @return pointful value of current <code>TextField</code>
	 */
	public String getValue() {
		return getValueOf(getText());
	}

	@Override
	public void setText(String text) {
		String value = getValueOf(text);

		if (value == null)
			setPlaceholder();
		else {
			setForeground(theme.getFocus());
			super.setText(value);
		}
	}

	private void setPlaceholder() {
		setForeground(theme.getFocusLost());
		super.setText(placeholder);
	}

	private void setEmpty() {
		setForeground(theme.getFocus());
		super.setText("");
	}

	void updateStyle() {
		setForeground(getValue() == null ? theme.getFocusLost() : theme
				.getFocus());
	}

	/**
	 * The shorthand for setText(obj.toString());
	 * 
	 * @param obj
	 *            Object to be set as text
	 */
	public void setValue(Object obj) {
		this.setText(obj == null ? null : obj.toString());
	}

	protected void setValue(String s) {
		this.setText(s);
	}

	public String getPlaceholder() {
		return placeholder;
	}

	protected void setPlaceholder(String placeholder) {
		this.oldPlaceholder = this.placeholder;
		this.placeholder = placeholder;
		if (getValue() == null)
			setPlaceholder();
	}

	CenterPanelTheme getTheme() {
		return theme;
	}

	protected void setTheme(CenterPanelTheme theme) {
		if (theme == null)
			theme = CenterPanel.defaultTheme;

		this.theme = theme;
		updateStyle();
	}

	protected void onFocusGained() {
		if (getValue() == null)
			setEmpty();
	}

	protected void onFocusLost() {
		if (getValue() == null)
			setPlaceholder();
	}

	protected void onChange() {
	}
}
