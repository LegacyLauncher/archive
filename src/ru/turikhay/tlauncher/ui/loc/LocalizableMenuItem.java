package ru.turikhay.tlauncher.ui.loc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

public class LocalizableMenuItem extends JMenuItem implements
		LocalizableComponent {
	private static final long serialVersionUID = 1364363532569997394L;
	private static List<LocalizableMenuItem> items = Collections
			.synchronizedList(new ArrayList<LocalizableMenuItem>());

	private String path;
	private String[] variables;

	public LocalizableMenuItem(String path, Object... vars) {
		super();

		items.add(this);
		setText(path, vars);
	}

	public LocalizableMenuItem(String path) {
		this(path, Localizable.EMPTY_VARS);
	}

	public void setText(String path, Object... vars) {
		this.path = path;
		this.variables = Localizable.checkVariables(vars);

		String value = Localizable.get(path);
		for (int i = 0; i < variables.length; i++)
			value = value.replace("%" + i, variables[i]);

		super.setText(value);
	}

	@Override
	public void setText(String path) {
		setText(path, Localizable.EMPTY_VARS);
	}

	public void setVariables(Object... vars) {
		setText(path, vars);
	}

	@Override
	public void updateLocale() {
		setText(path, (Object[]) variables);
	}

	public static void updateLocales() {
		for (LocalizableMenuItem item : items) {
			if (item == null)
				continue;
			item.updateLocale();
		}
	}

}
