package ru.turikhay.tlauncher.ui.editor;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.FocusListener;

import ru.turikhay.tlauncher.ui.block.Blocker;

public class EditorFieldHandler extends EditorHandler {
	private final EditorField field;
	private final Component component;

	public EditorFieldHandler(String path, Component comp, FocusListener focus) {
		super(path);

		if (!(comp instanceof EditorField))
			throw new IllegalArgumentException();

		if (focus != null)
			addFocus(comp, focus);

		this.component = comp;
		this.field = (EditorField) comp;
	}

	public EditorFieldHandler(String path, Component comp) {
		this(path, comp, null);
	}

	@Override
	public Component getComponent() {
		return component;
	}

	@Override
	public String getValue() {
		return field.getSettingsValue();
	}

	@Override
	protected void setValue0(String s) {
		field.setSettingsValue(s);
	}

	@Override
	public boolean isValid() {
		return field.isValueValid();
	}

	private void addFocus(Component comp, FocusListener focus) {
		comp.addFocusListener(focus);

		if (comp instanceof Container)
			for (Component curComp : ((Container) comp).getComponents())
				addFocus(curComp, focus);
	}

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(reason, getComponent());
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(reason, getComponent());
	}
}
