package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.HtmlSubstitutor;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;
import java.util.Objects;

public class AccountSuccess extends EditorPane implements LocalizableComponent, AccountMultipaneComp {
    private final PaneMode mode;

    public AccountSuccess(PaneMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
        updateLocale();
    }

    @Override
    public void updateLocale() {
        setText(TokenReplacingReader.resolveVars(Localizable.get("account.manager.multipane.success.body"), new HtmlSubstitutor()));
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "success-" + mode.toString().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean multipaneLocksView() {
        return false;
    }

    @Override
    public void multipaneShown(boolean gotBack) {

    }
}
