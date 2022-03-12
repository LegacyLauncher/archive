package ru.turikhay.tlauncher.ui.settings;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.editor.EditorIntegerField;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsMemorySlider extends BorderPanel implements EditorField {
    private final JSlider slider = new JSlider();
    private final EditorIntegerField inputField;
    private final LocalizableLabel hint;

    SettingsMemorySlider(SettingsPanel s) {
        slider.setOpaque(false);
        slider.setMinimum(OS.Arch.x64.isCurrent() ? 1024 : 512);
        slider.setMaximum(OS.Arch.MAX_MEMORY);
        slider.setMinorTickSpacing(OS.Arch.x64.isCurrent() ? 1024 : 256);
        slider.setMajorTickSpacing(OS.Arch.x64.isCurrent() ? (OS.Arch.MAX_MEMORY > 8196 ? OS.Arch.MAX_MEMORY / 4096 * 1024 : 1024) : 512);
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                requestFocusInWindow();
            }
        });
        setCenter(slider);
        inputField = new EditorIntegerField();
        inputField.textField.setColumns(4);
        LocalizableLabel mb = new LocalizableLabel("settings.java.memory.mb");
        ExtendedPanel panel = new ExtendedPanel();
        panel.add(inputField, mb);
        setEast(panel);
        hint = new LocalizableHTMLLabel("");
        setSouth(hint);
        slider.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                onSliderUpdate();
            }
        });
        slider.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                onSliderUpdate();
            }
        });
        inputField.textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateInfo();
            }

            public void removeUpdate(DocumentEvent e) {
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    public void setBackground(Color color) {
        if (inputField != null) {
            inputField.textField.setBackground(color);
        }
    }

    public void block(Object reason) {
        Blocker.blockComponents(reason, slider, inputField, hint);
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(reason, slider, inputField, hint);
    }

    public String getSettingsValue() {
        return inputField.textField.getValue();
    }

    public void setSettingsValue(String value) {
        inputField.textField.setValue(value);
        updateInfo();
    }

    public boolean isValueValid() {
        return inputField.getIntegerValue() >= 512;
    }

    private void onSliderUpdate() {
        inputField.textField.setValue(slider.getValue());
        updateTip();
    }

    private void updateSlider() {
        int intVal = inputField.getIntegerValue();
        if (intVal > 1) {
            slider.setValue(intVal);
        }

    }

    private void updateTip() {
        SettingsMemorySlider.ValueType value = null;

        int intVal = inputField.getIntegerValue();
        if (intVal < OS.Arch.MIN_MEMORY) {
            value = SettingsMemorySlider.ValueType.DANGER;
        } else if (intVal == OS.Arch.PREFERRED_MEMORY) {
            value = SettingsMemorySlider.ValueType.OK;
        } else {
            switch (OS.Arch.CURRENT) {
                case x86:
                    if (OS.Arch.TOTAL_RAM_MB > 0L && (long) intVal > OS.Arch.TOTAL_RAM_MB) {
                        value = SettingsMemorySlider.ValueType.DANGER;
                    } else if (intVal > OS.Arch.MAX_MEMORY) {
                        value = SettingsMemorySlider.ValueType.WARNING;
                    }
                    break;
                default:
                    if (intVal > OS.Arch.TOTAL_RAM_MB) {
                        value = SettingsMemorySlider.ValueType.DANGER;
                    }
            }
        }

        String path;
        ImageIcon icon;
        if (value == null) {
            path = "";
            icon = null;
        } else {
            path = value.path;
            icon = value.icon;
        }

        hint.setText(path);
        ImageIcon.setup(hint, icon);
    }

    public void updateInfo() {
        updateSlider();
        updateTip();
    }

    private enum ValueType {
        OK("info-circle"),
        WARNING("warning"),
        DANGER("warning-2");

        private final String path = "settings.java.memory.hint." + toString().toLowerCase(java.util.Locale.ROOT);
        private final ImageIcon icon;

        ValueType(String image) {
            icon = Images.getIcon16(image);
        }
    }
}
