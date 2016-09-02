package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.block.Blocker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusListener;

public class EditorFieldHandler extends EditorHandler {
    private final EditorField field;
    private final JComponent comp;

    public EditorFieldHandler(String path, JComponent component, FocusListener focus) {
        super(path);
        if (component == null) {
            throw new NullPointerException("comp");
        } else if (!(component instanceof EditorField)) {
            throw new IllegalArgumentException();
        } else {
            if (focus != null) {
                addFocus(component, focus);
            }

            comp = component;
            field = (EditorField) component;
        }
    }

    public EditorFieldHandler(String path, JComponent comp) {
        this(path, comp, null);
    }

    public JComponent getComponent() {
        return comp;
    }

    public String getValue() {
        return field.getSettingsValue();
    }

    protected void setValue0(String s) {
        field.setSettingsValue(s);
    }

    public boolean isValid() {
        return field.isValueValid();
    }

    private void addFocus(Component comp, FocusListener focus) {
        comp.addFocusListener(focus);
        if (comp instanceof Container) {
            Component[] var6;
            int var5 = (var6 = ((Container) comp).getComponents()).length;

            for (int var4 = 0; var4 < var5; ++var4) {
                Component curComp = var6[var4];
                addFocus(curComp, focus);
            }
        }

    }

    public void block(Object reason) {
        Blocker.blockComponents(reason, getComponent());
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(reason, getComponent());
    }
}
