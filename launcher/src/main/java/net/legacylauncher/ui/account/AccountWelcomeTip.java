package net.legacylauncher.ui.account;

import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.editor.EditorPane;
import net.legacylauncher.ui.swing.extended.HtmlSubstitutor;
import net.legacylauncher.util.git.TokenReplacingReader;

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
