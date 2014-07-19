package ru.turikhay.tlauncher.ui.settings;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.editor.EditorIntegerField;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;

public class SettingsMemorySlider extends BorderPanel implements EditorField {

	private final JSlider slider;
	private final EditorIntegerField inputField;
	private final LocalizableLabel mb, hint;

	SettingsMemorySlider() {
		this.slider = new JSlider();
		slider.setOpaque(false);

		slider.setMinimum(OS.Arch.MIN_MEMORY);
		slider.setMaximum(OS.Arch.MAX_MEMORY);
		slider.setMinorTickSpacing(OS.Arch.x64.isCurrent()? 256 : 128);
		slider.setMajorTickSpacing(512);
		slider.setSnapToTicks(true);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);

		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				requestFocusInWindow();
			}
		});

		setCenter(slider);

		this.inputField = new EditorIntegerField();
		inputField.setColumns(5);

		this.mb = new LocalizableLabel("settings.java.memory.mb");

		ExtendedPanel panel = new ExtendedPanel();
		panel.add(inputField, mb);

		setEast(panel);

		this.hint = new LocalizableHTMLLabel("");
		setSouth(hint);

		slider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				onSliderUpdate();
			}
		});

		slider.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				onSliderUpdate();
			}
		});

		inputField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateInfo();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
			}
		});
	}

	@Override
	public void setBackground(Color color) {
		if(inputField != null)
			inputField.setBackground(color);
	}

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(reason, slider, inputField, hint);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(reason, slider, inputField, hint);
	}

	@Override
	public String getSettingsValue() {
		return inputField.getValue();
	}

	@Override
	public void setSettingsValue(String value) {
		inputField.setValue(value);
		updateInfo();
	}

	@Override
	public boolean isValueValid() {
		return inputField.getIntegerValue() >= OS.Arch.MIN_MEMORY;
	}

	private void onSliderUpdate() {
		inputField.setValue(slider.getValue());
		updateTip();
	}

	private void updateSlider() {
		int intVal = inputField.getIntegerValue();

		if(intVal > 1)
			slider.setValue(intVal);
	}

	private void updateTip() {
		int intVal = inputField.getIntegerValue();
		ValueType value = null;

		if(intVal < OS.Arch.MIN_MEMORY)
			value = ValueType.DANGER;
		else if(intVal == OS.Arch.PREFERRED_MEMORY)
			value = ValueType.OK;
		else
			switch(OS.Arch.CURRENT) {
			case x64:

				if(OS.Arch.TOTAL_RAM_MB > 0 && intVal > OS.Arch.TOTAL_RAM_MB)
					value = ValueType.DANGER;
				else if(intVal > OS.Arch.MAX_MEMORY)
					value = ValueType.WARNING;

				break;
			default:
				if(intVal > OS.Arch.MAX_MEMORY)
					value = ValueType.DANGER;
				else if(intVal > OS.Arch.PREFERRED_MEMORY)
					value = ValueType.WARNING;
			}

		String path; ImageIcon icon;

		if(value == null) {
			path = "";
			icon = null;
		} else {
			path = value.path;
			icon = value.icon;
		}

		hint.setText(path);
		ImageIcon.setup(hint, icon);
	}

	private void updateInfo() {
		updateSlider();
		updateTip();
	}

	private enum ValueType {
		OK("info.png"), WARNING("warning.png"), DANGER("danger.png");

		private final String path;
		private final ImageIcon icon;

		ValueType(String image) {
			this.path = "settings.java.memory.hint."+ toString().toLowerCase();
			this.icon = ImageCache.getIcon(image, 16, 16);
		}
	}

}
