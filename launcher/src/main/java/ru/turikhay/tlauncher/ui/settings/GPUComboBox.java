package ru.turikhay.tlauncher.ui.settings;

import ru.turikhay.tlauncher.managers.GPUManager;
import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.editor.EditorComboBox;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.ConverterCellRenderer;
import ru.turikhay.tlauncher.ui.swing.DefaultConverterCellRenderer;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPUComboBox extends BorderPanel implements EditorField {
    private final SettingsPanel sp;
    private final EditorComboBox<GPUManager.GPU> comboBox;

    public GPUComboBox(SettingsPanel sp) {
        this.sp = sp;

        GPUManager gpuManager = sp.tlauncher.getGpuManager();
        List<GPUManager.GPU> gpus = gpuManager.discoveryGPUs();
        comboBox = new EditorComboBox<>(new GPUCellRenderer(gpus, gpuManager), false);
        GPUManager.GPU.GLOBAL_DEFINED.forEach(comboBox::addItem);
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

    private static class GPUConverter implements StringConverter<GPUManager.GPU> {

        private final List<GPUManager.GPU> gpus;
        private final GPUManager gpuManager;

        GPUConverter(List<GPUManager.GPU> gpus, GPUManager gpuManager) {
            this.gpus = gpus;
            this.gpuManager = gpuManager;
        }

        @Override
        public GPUManager.GPU fromString(String name) {
            return GPUManager.GPU.GLOBAL_DEFINED.stream().filter(gpu -> gpu.getName().equalsIgnoreCase(name)).findAny()
                    .orElseGet(() -> gpus.stream().filter(gpu -> gpu.getName().equalsIgnoreCase(name)).findAny().orElse(null));
        }

        @Override
        public String toString(GPUManager.GPU gpu) {
            return gpu.getDisplayName(gpuManager);
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

    private static class GPUCellRenderer extends ConverterCellRenderer<GPUManager.GPU> {
        private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

        GPUCellRenderer(List<GPUManager.GPU> gpus, GPUManager gpuManager) {
            super(new GPUConverter(gpus, gpuManager));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends GPUManager.GPU> list, GPUManager.GPU value, int index, boolean isSelected, boolean cellHasFocus) {
            final JLabel label = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(converter.toString(value));
            final ImageIcon icon;
            switch (value.getVendor()) {
                case AMD:
                    icon = Images.getIcon16("gpu-icon-amd");
                    break;
                case Nvidia:
                    icon = Images.getIcon16("gpu-icon-nvidia");
                    break;
                case Intel:
                    icon = Images.getIcon16("gpu-icon-intel");
                    break;
                case Unknown:
                default:
                    icon = null;
                    break;
            }
            label.setIcon(icon);
            return label;
        }
    }
}
