package ru.turikhay.tlauncher.ui.settings;

import ru.turikhay.tlauncher.managers.GPUManager;
import ru.turikhay.tlauncher.minecraft.launcher.ProcessHook;
import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.editor.EditorComboBox;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPUComboBox extends BorderPanel implements EditorField {
    private final SettingsPanel sp;
    private final EditorComboBox<GPUManager.GPU> comboBox;

    public GPUComboBox(SettingsPanel sp) {
        this.sp = sp;

        List<GPUManager.GPU> gpus = sp.tlauncher.getGpuManager().discoveryGPUs();
        comboBox = new EditorComboBox<>(new GPUConverter(gpus), null);
        comboBox.addItem(DEFAULT_GPU);
        gpus.forEach(comboBox::addItem);

        setCenter(comboBox);
    }

    @Override
    public void block(Object reason) {
        comboBox.block(reason);
    }

    @Override
    public void unblock(Object reason) {
        comboBox.unblock(reason);
    }

    @Override
    public String getSettingsValue() {
        return comboBox.getSettingsValue();
    }

    @Override
    public void setSettingsValue(String value) {
        comboBox.setSettingsValue(value);
    }

    @Override
    public boolean isValueValid() {
        if (!comboBox.isValueValid()) {
            return false;
        }
        return true;
    }

    private static final GPUManager.GPU DEFAULT_GPU = new GPUManager.GPU("SYSTEM", true, ProcessHook.None.INSTANCE);

    private static class GPUConverter implements StringConverter<GPUManager.GPU> {
        private static final Pattern PATTERN = Pattern.compile(".*\\[([^]]+)]");

        private final List<GPUManager.GPU> gpus;

        public GPUConverter(List<GPUManager.GPU> gpus) {
            this.gpus = gpus;
        }

        @Override
        public GPUManager.GPU fromString(String name) {
            if (DEFAULT_GPU.getName().equals(name)) return DEFAULT_GPU;
            return gpus.stream().filter(gpu -> gpu.getName().equalsIgnoreCase(name)).findAny().orElse(null);
        }

        @Override
        public String toString(GPUManager.GPU gpu) {
            if (gpu == DEFAULT_GPU) {
                return Localizable.get("settings.gpu.default.label");
            }
            String name = gpu.getName();
            Matcher matcher = PATTERN.matcher(name);
            if (matcher.matches()) {
                return matcher.group(1);
            } else {
                return name;
            }
        }

        @Override
        public String toValue(GPUManager.GPU gpu) {
            return gpu.getName();
        }

        @Override
        public Class<GPUManager.GPU> getObjectClass() {
            return GPUManager.GPU.class;
        }
    }
}
