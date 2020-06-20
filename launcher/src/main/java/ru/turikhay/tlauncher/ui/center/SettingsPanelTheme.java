package ru.turikhay.tlauncher.ui.center;

import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.OS;

import java.awt.*;

public class SettingsPanelTheme extends DefaultCenterPanelTheme {
    protected Theme.Border getBorderType() {
        return Theme.Border.SETTINGS_PANEL;
    }
}
