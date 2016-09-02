package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedFrame;

import javax.swing.*;

public abstract class ActionFrame extends ExtendedFrame implements LocalizableComponent {
    private String title;
    private Object[] titleVars;

    public final String getTitlePath() {
        return title;
    }

    public final Object[] getTitleVars() {
        return titleVars;
    }

    public final void setTitlePath(String title, Object... vars) {
        this.title = title;
        titleVars = vars;
        updateTitle();
    }

    public abstract JComponent getHead();

    public abstract JComponent getBody();

    public abstract JComponent getFooter();

    @Override
    public void updateLocale() {
        updateTitle();
        Localizable.updateContainer(this);
    }

    private void updateTitle() {
        setTitle(Localizable.get(title, titleVars));
    }
}
