package net.legacylauncher.ui.scenes;

import net.legacylauncher.ui.MainPane;
import net.legacylauncher.ui.swing.AnimatedVisibility;
import net.legacylauncher.ui.swing.extended.ExtendedLayeredPane;

public abstract class PseudoScene extends ExtendedLayeredPane implements AnimatedVisibility {
    private static final long serialVersionUID = -1L;
    private final MainPane main;
    private boolean shown = true;

    protected PseudoScene(MainPane main) {
        super(main);
        this.main = main;
        setSize(main.getWidth(), main.getHeight());
    }

    public MainPane getMainPane() {
        return main;
    }

    public void setShown(boolean shown) {
        setShown(shown, true);
    }

    public void setShown(boolean shown, boolean animate) {
        if (this.shown != shown) {
            this.shown = shown;
            setVisible(shown);
        }
    }
}
