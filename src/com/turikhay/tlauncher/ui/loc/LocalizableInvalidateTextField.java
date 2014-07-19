package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.text.InvalidateTextField;

public class LocalizableInvalidateTextField extends InvalidateTextField
		implements LocalizableComponent {
	private static final long serialVersionUID = -3999545292427982797L;

	private String placeholderPath;

	private LocalizableInvalidateTextField(CenterPanel panel,
			String placeholderPath, String value) {
		super(panel, null, value);

		this.placeholderPath = placeholderPath;
		this.setValue(value);
	}

	protected LocalizableInvalidateTextField(String placeholderPath) {
		this(null, placeholderPath, null);
	}

	@Override
	public void setPlaceholder(String placeholderPath) {
		this.placeholderPath = placeholderPath;
		super.setPlaceholder((Localizable.get() == null) ? placeholderPath
				: Localizable.get().get(placeholderPath));
	}

	public String getPlaceholderPath() {
		return this.placeholderPath;
	}

	@Override
	public void updateLocale() {
		this.setPlaceholder(placeholderPath);
	}

}