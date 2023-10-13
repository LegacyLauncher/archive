package net.legacylauncher.ui.frames;

import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.extended.ExtendedFrame;

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
