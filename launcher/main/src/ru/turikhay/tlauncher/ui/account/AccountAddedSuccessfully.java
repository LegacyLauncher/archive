package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.HtmlSubstitutor;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;

public class AccountAddedSuccessfully extends EditorPane implements LocalizableComponent, AccountMultipaneComp {

    public AccountAddedSuccessfully() {
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
        return "success";
    }

    @Override
    public boolean multipaneLocksView() {
        return false;
    }

    @Override
    public void multipaneShown() {

    }
}
