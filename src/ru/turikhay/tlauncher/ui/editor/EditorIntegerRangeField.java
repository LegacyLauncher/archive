package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.util.Range;

public class EditorIntegerRangeField extends EditorIntegerField {

	private final Range<Integer> range;

	public EditorIntegerRangeField(Range<Integer> range) {
		if(range == null)
			throw new NullPointerException("range");

		this.range = range;
		this.setPlaceholder("settings.range", range.getMinValue(), range.getMaxValue());
	}

	@Override
	public boolean isValueValid() {
		try {
			return range.fits(Integer.parseInt(getSettingsValue()));
		} catch (Exception e) {
			return false;
		}
	}

}
