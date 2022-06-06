package ru.turikhay.tlauncher.ui.swing.combobox;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class IconTextComponent extends JTextField {
    private IconTextComponentHelper mHelper;

    public IconTextComponent() {
        super();
    }

    public IconTextComponent(int cols) {
        super(cols);
    }

    private IconTextComponentHelper getHelper() {
        if (mHelper == null) {
            mHelper = new IconTextComponentHelper(this);
        }
        return mHelper;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        getHelper().onPaintComponent(graphics);
    }

    public void setIcon(Icon icon) {
        getHelper().onSetIcon(icon);
    }

    @Override
    public void setBorder(Border border) {
        getHelper().onSetBorder(border);
        super.setBorder(getHelper().getBorder());
    }
}
