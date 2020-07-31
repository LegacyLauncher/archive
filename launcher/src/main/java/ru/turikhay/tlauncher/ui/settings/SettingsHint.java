package ru.turikhay.tlauncher.ui.settings;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.editor.EditorHandler;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

public class SettingsHint extends EditorHandler implements LocalizableComponent {
    private final EditorPane label;

    public SettingsHint(String path) {
        super(path);
        this.label = new EditorPane(new LocalizableLabel().getFont().deriveFont(SwingUtil.magnify(TLauncherFrame.getFontSize() * .8f)));
        label.setMargin(new Insets(0, SwingUtil.magnify(5), 0, 0));
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return label;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    protected void setValue0(String var1) {
    }

    @Override
    public void block(Object var1) {
        label.setEnabled(false);
    }

    @Override
    public void unblock(Object var1) {
        label.setEnabled(true);
    }

    @Override
    public void updateLocale() {
        label.setText(Localizable.get(getPath()));
    }
}
