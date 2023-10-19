package net.legacylauncher.ui.loc;

import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.center.CenterPanel;
import net.legacylauncher.ui.text.ExtendedTextField;

public class LocalizableTextField extends ExtendedTextField implements LocalizableComponent {
    private static final long serialVersionUID = 359096767189321072L;
    protected String placeholderPath;
    protected String[] variables;

    public LocalizableTextField(CenterPanel panel, String placeholderPath, String value) {
        super(panel, null, value);
        setValue(value);
        setPlaceholder(placeholderPath);
        setFont(getFont().deriveFont(LegacyLauncherFrame.getFontSize()));
    }

    public LocalizableTextField(CenterPanel panel, String placeholderPath) {
        this(panel, placeholderPath, null);
    }

    public LocalizableTextField(String placeholderPath) {
        this(null, placeholderPath, null);
    }

    public LocalizableTextField() {
        this(null, null, null);
    }

    public void setPlaceholder(String placeholderPath, Object... vars) {
        this.placeholderPath = placeholderPath;
        variables = Localizable.checkVariables(vars);
        String value = Localizable.get(placeholderPath);

        for (int i = 0; i < variables.length; ++i) {
            value = value.replace("%" + i, variables[i]);
        }

        super.setPlaceholder(value);
    }

    public void setPlaceholder(String placeholderPath) {
        setPlaceholder(placeholderPath, Localizable.EMPTY_VARS);
    }

    public String getPlaceholderPath() {
        return placeholderPath;
    }

    public void updateLocale() {
        setPlaceholder(placeholderPath, (Object[]) variables);
    }
}
