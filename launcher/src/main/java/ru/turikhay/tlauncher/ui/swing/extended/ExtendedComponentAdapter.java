package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.*;

public class ExtendedComponentAdapter extends ExtendedComponentListener {
    public ExtendedComponentAdapter(Component component, int tick) {
        super(component, tick);
    }

    public ExtendedComponentAdapter(Component component) {
        super(component);
    }

}
