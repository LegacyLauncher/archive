package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.SwingConstants;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.IntegerArray;

public class EditorResolutionField extends ExtendedPanel implements
		EditorField {
	private static final long serialVersionUID = -5565607141889620750L;

	private EditorIntegerField w;
	private EditorIntegerField h;
	private ExtendedLabel x;

	private final int[] defaults;

	public EditorResolutionField() {
		this(null, null, new int[] { 0, 0 });
	}

	public EditorResolutionField(String promptW, String promptH,
			Configuration settings) {
		this(promptW, promptH, settings.getDefaultWindowSize());
	}

	public EditorResolutionField(String promptW, String promptH, int[] defaults) {
		if (defaults == null)
			throw new NullPointerException();
		if (defaults.length != 2)
			throw new IllegalArgumentException("Illegal array size");

		this.defaults = defaults;

		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setAlignmentY(CENTER_ALIGNMENT);

		this.w = new EditorIntegerField(promptW);
		w.setColumns(4);
		w.setHorizontalAlignment(SwingConstants.CENTER);

		this.h = new EditorIntegerField(promptH);
		h.setColumns(4);
		h.setHorizontalAlignment(SwingConstants.CENTER);

		this.x = new ExtendedLabel("X", SwingConstants.CENTER);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;

		c.gridx = 0;
		c.weightx = 0.5;
		c.insets.set(0, 0, 0, 0);
		c.fill = GridBagConstraints.BOTH;
		add(w, c);

		c.gridx = 1;
		c.weightx = 0;
		c.insets.set(0, 5, 0, 5);
		c.fill = GridBagConstraints.VERTICAL;
		add(x, c);

		c.gridx = 2;
		c.weightx = 0.5;
		c.insets.set(0, 0, 0, 0);
		c.fill = GridBagConstraints.BOTH;
		add(h, c);
	}

	@Override
	public String getSettingsValue() {
		return w.getSettingsValue() + IntegerArray.defaultDelimiter
				+ h.getSettingsValue();
	}

	int[] getResolution() {
		try {
			IntegerArray arr = IntegerArray
					.parseIntegerArray(getSettingsValue());
			return arr.toArray();
		} catch (Exception e) {
			return new int[2];
		}
	}

	@Override
	public boolean isValueValid() {
		int[] size = getResolution();

		return size[0] >= defaults[0] && size[1] >= defaults[1];
	}

	@Override
	public void setSettingsValue(String value) {
		String width, height;

		try {
			IntegerArray arr = IntegerArray.parseIntegerArray(value);
			width = String.valueOf(arr.get(0));
			height = String.valueOf(arr.get(1));
		} catch (Exception e) {
			width = "";
			height = "";
		}

		w.setText(width);
		h.setText(height);
	}

	@Override
	public void setBackground(Color bg) {
		if (w != null)
			w.setBackground(bg);
		if (h != null)
			h.setBackground(bg);
	}

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(reason, w, h);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, w, h);
	}

}
