package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.HtmlSubstitutor;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;

public class AccountWelcomeTip extends EditorPane implements LocalizableComponent, AccountMultipaneComp {

    public AccountWelcomeTip() {
        updateLocale();
    }

    @Override
    public void updateLocale() {
        setText(TokenReplacingReader.resolveVars(Localizable.get("account.manager.multipane.welcome.body"), new HtmlSubstitutor()));
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "welcome";
    }

    @Override
    public boolean multipaneLocksView() {
        return false;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
    }
}
