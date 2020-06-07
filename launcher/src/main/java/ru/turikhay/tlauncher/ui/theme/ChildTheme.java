package ru.turikhay.tlauncher.ui.theme;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

public abstract class ChildTheme extends Theme {
    private final SystemTheme system = SystemTheme.getSystemTheme();

    ChildTheme(String name) {
        super(name);
    }

    @Override
    public Color getForeground() {
        return system.getForeground();
    }

    @Override
    public Color getSemiForeground() {
        return system.getSemiForeground();
    }

    @Override
    public Color getBackground() {
        return system.getBackground();
    }

    @Override
    public Color getPanelBackground() {
        return system.getPanelBackground();
    }

    @Override
    public Color getSuccess() {
        return system.getSuccess();
    }

    @Override
    public Color getFailure() {
        return system.getFailure();
    }

    @Override
    public int getBorderSize() {
        return system.getBorderSize();
    }

    @Override
    public Color getBorder(Border border) {
        return system.getBorder(border);
    }

    @Override
    public Color getShadow(Border border) {
        return system.getShadow(border);
    }

    @Override
    public int getArc(Border border) {
        return system.getArc(border);
    }

    @Override
    public URL loadAsset(String name) throws IOException {
        return system.loadAsset(name);
    }
}
