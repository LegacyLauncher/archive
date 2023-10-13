package net.legacylauncher.ui.editor;

import net.legacylauncher.ui.block.Blockable;

public interface EditorField extends Blockable {
    String getSettingsValue();

    void setSettingsValue(String var1);

    boolean isValueValid();
}
