package ru.turikhay.tlauncher.ui.swing;

import java.awt.*;

public interface DelayedComponentLoader<T extends Component> {
    T loadComponent();

    void onComponentLoaded(T loaded);
}
