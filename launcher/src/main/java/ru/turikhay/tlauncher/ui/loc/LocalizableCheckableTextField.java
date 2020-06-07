package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.text.CheckableTextField;

public abstract class LocalizableCheckableTextField extends CheckableTextField implements LocalizableComponent {
    private static final long serialVersionUID = 1L;
    private String placeholderPath;

    private LocalizableCheckableTextField(CenterPanel panel, String placeholderPath, String value) {
        super(panel, null, null);
        this.placeholderPath = placeholderPath;
        setValue(value);
    }

    public LocalizableCheckableTextField(CenterPanel panel, String placeholderPath) {
        this(panel, placeholderPath, null);
    }

    public LocalizableCheckableTextField(String placeholderPath, String value) {
        this(null, placeholderPath, value);
    }

    public LocalizableCheckableTextField(String placeholderPath) {
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
