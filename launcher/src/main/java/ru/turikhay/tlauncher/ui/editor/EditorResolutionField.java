package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.IntegerArray;

import java.awt.*;

public class EditorResolutionField extends BorderPanel implements EditorField {
    private static final long serialVersionUID = -5565607141889620750L;
    private final EditorIntegerField w;
    private final EditorIntegerField h;

    public EditorResolutionField(String promptW, String promptH, int[] defaults, boolean showDefault) {
        if (defaults == null) {
            throw new NullPointerException();
        } else if (defaults.length != 2) {
            throw new IllegalArgumentException("Illegal array size");
        } else {
            ExtendedPanel container = new ExtendedPanel();
            container.setAlignmentX(0.5F);
            container.setAlignmentY(0.5F);
            w = new EditorIntegerField(promptW);
            w.textField.setColumns(4);
            w.textField.setHorizontalAlignment(0);
            h = new EditorIntegerField(promptH);
            h.textField.setColumns(4);
            h.textField.setHorizontalAlignment(0);
            ExtendedLabel x = new ExtendedLabel("X", 0);
            container.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = 10;
            c.gridx = 0;
            c.weightx = 0.5D;
            c.insets.set(0, 0, 0, 0);
            c.fill = 1;
            container.add(w, c);
            c.gridx = 1;
            c.weightx = 0.0D;
            c.insets.set(0, 5, 0, 5);
            c.fill = 3;
            container.add(x, c);
            c.gridx = 2;
            c.weightx = 0.5D;
            c.insets.set(0, 0, 0, 0);
            c.fill = 1;
            container.add(h, c);
            setCenter(container);
            LocalizableLabel hint = new LocalizableHTMLLabel("settings.res.def", defaults[0], defaults[1]);
            hint.setFont(hint.getFont().deriveFont((float) hint.getFont().getSize() - 2.0F));
            if (showDefault) {
                setSouth(hint);
            }

        }
    }

    public String getSettingsValue() {
        return w.getSettingsValue() + ';' + h.getSettingsValue();
    }

    int[] getResolution() {
        try {
            IntegerArray e = IntegerArray.parseIntegerArray(getSettingsValue());
            return e.toArray();
        } catch (Exception var2) {
            return new int[2];
        }
    }

    public boolean isValueValid() {
        int[] size = getResolution();
        return size[0] >= 1 && size[1] >= 1;
    }

    public void setSettingsValue(String value) {
        String width;
        String height;
        try {
            IntegerArray e = IntegerArray.parseIntegerArray(value);
            width = String.valueOf(e.get(0));
            height = String.valueOf(e.get(1));
        } catch (Exception var5) {
            width = "";
            height = "";
        }

        w.textField.setText(width);
        h.textField.setText(height);
    }

    public void setBackground(Color bg) {
        if (w != null) {
            w.setBackground(bg);
        }

        if (h != null) {
            h.setBackground(bg);
        }

    }

    public void block(Object reason) {
        Blocker.blockComponents(reason, w, h);
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, w, h);
    }
}
