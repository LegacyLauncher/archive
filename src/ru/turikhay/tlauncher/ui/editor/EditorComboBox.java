package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;

public class EditorComboBox<T> extends ExtendedComboBox<T> implements
		EditorField {
	private static final long serialVersionUID = -2320340434786516374L;
	private final boolean allowNull;

	public EditorComboBox(StringConverter<T> converter, T[] values, boolean allowNull) {
		super(converter);
		
		this.allowNull = allowNull;

		if (values == null)
			return;

		for (T value : values)
			addItem(value);
	}
	
	public EditorComboBox(StringConverter<T> converter, T[] values) {
		this(converter, values, false);
	}

	@Override
	public String getSettingsValue() {
		T value = getSelectedValue();
		return convert(value);
	}

	@Override
	public void setSettingsValue(String string) {
		T value = convert(string);
		
		if(!allowNull && string == null) {
			boolean hasNull = false;
			
			for(int i=0;i<getItemCount();i++)
				if(getItemAt(i) == null)
					hasNull = true;
			
			if(!hasNull) return;
		}
		
		setSelectedValue(value);
	}

	@Override
	public boolean isValueValid() {
		return true;
	}

	@Override
	public void block(Object reason) {
		this.setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		this.setEnabled(true);
	}

}
