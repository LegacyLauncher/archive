package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.block.Blockable;

public interface EditorField extends Blockable {
    String getSettingsValue();

    void setSettingsValue(String var1);

    boolean isValueValid();
}
