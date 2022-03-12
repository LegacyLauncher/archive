package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.util.Range;

public class EditorIntegerRangeField extends EditorIntegerField {
    private final Range<Integer> range;

    public EditorIntegerRangeField(String placeholder, Range<Integer> range) {
        if (range == null) {
            throw new NullPointerException("range");
        } else {
            this.range = range;
            textField.setPlaceholder(placeholder);
        }
    }

    public EditorIntegerRangeField(Range<Integer> range) {
        this(null, range);
        textField.setPlaceholder("settings.range", range.getMinValue(), range.getMaxValue());
    }

    public boolean isValueValid() {
        try {
            return range.fits(Integer.valueOf(Integer.parseInt(getSettingsValue())));
        } catch (Exception var2) {
            return false;
        }
    }
}
