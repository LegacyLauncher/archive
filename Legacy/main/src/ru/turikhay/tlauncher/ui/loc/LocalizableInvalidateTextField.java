package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.text.InvalidateTextField;

public class LocalizableInvalidateTextField extends InvalidateTextField implements LocalizableComponent {
    private static final long serialVersionUID = -3999545292427982797L;
    private String placeholderPath;

    private LocalizableInvalidateTextField(CenterPanel panel, String placeholderPath, String value) {
        super(panel, null, value);
        this.placeholderPath = placeholderPath;
        setValue(value);
    }

    protected LocalizableInvalidateTextField(String placeholderPath) {
        this(null, placeholderPath, null);
    }

    public void setPlaceholder(String placeholderPath) {
        this.placeholderPath = placeholderPath;
        super.setPlaceholder(Localizable.get() == null ? placeholderPath : Localizable.get().get(placeholderPath));
    }

    public String getPlaceholderPath() {
        return placeholderPath;
    }

    public void updateLocale() {
        setPlaceholder(placeholderPath);
    }
}
