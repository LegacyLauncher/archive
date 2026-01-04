package net.legacylauncher.ui.settings;

import lombok.Getter;
import net.legacylauncher.managers.GPUManager;
import net.legacylauncher.ui.converter.StringConverter;
import net.legacylauncher.ui.editor.EditorComboBox;
import net.legacylauncher.ui.editor.EditorField;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.ConverterCellRenderer;
import net.legacylauncher.ui.swing.extended.BorderPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GPUComboBox extends BorderPanel implements EditorField, LocalizableComponent {
    private final EditorComboBox<GPUManager.GPU> comboBox;

    public GPUComboBox(SettingsPanel sp) {
        GPUManager gpuManager = sp.tlauncher.getGpuManager();
        List<GPUManager.GPU> gpus = gpuManager.discoveryGPUs();
        GPUCellRenderer renderer = new GPUCellRenderer(gpus, gpuManager);
        comboBox = new EditorComboBox<>(renderer.getConverter(), renderer, false);
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
        return comboBox.isValueValid();
    }

    @Override
    public void updateLocale() {

    }

    private static class GPUConverter implements StringConverter<GPUManager.GPU> {

        private final List<GPUManager.GPU> gpus;
        private final GPUManager gpuManager;

        GPUConverter(List<GPUManager.GPU> gpus, GPUManager gpuManager) {
            this.gpus = gpus;
            this.gpuManager = gpuManager;
        }

        @Override
        public GPUManager.GPU fromString(String from) {
            return GPUManager.GPU.GLOBAL_DEFINED.stream().filter(gpu -> gpu.getName().equalsIgnoreCase(from)).findAny()
                    .orElseGet(() -> gpus.stream().filter(gpu -> gpu.getName().equalsIgnoreCase(from)).findAny().orElse(null));
        }

        @Override
        public String toString(GPUManager.GPU from) {
            if (from == null) {
                return "";
            }
            return from.getDisplayName(gpuManager);
        }

        @Override
        public String toValue(GPUManager.GPU from) {
            if (from == null) {
                return "";
            }
            return from.getName();
        }

        @Override
        public Class<GPUManager.GPU> getObjectClass() {
            return GPUManager.GPU.class;
        }
    }

    private static class GPUCellRenderer extends DefaultListCellRenderer implements ConverterCellRenderer<GPUManager.GPU> {
        @Getter
        private final StringConverter<GPUManager.GPU> converter;
        private final GPUManager gpuManager;

        GPUCellRenderer(List<GPUManager.GPU> gpus, GPUManager gpuManager) {
            this.gpuManager = gpuManager;
            converter = new GPUConverter(gpus, gpuManager);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            GPUManager.GPU gpu = (GPUManager.GPU) value;
            String text = converter.toString(gpu);
            super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            setIconTextGap(2);
            final ImageIcon icon;
            switch (gpu == null ? GPUManager.Vendor.Unknown : gpu.getVendor(gpuManager)) {
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
            setIcon(icon);
            return this;
        }
    }
}
