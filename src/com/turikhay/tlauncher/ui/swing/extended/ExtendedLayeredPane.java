package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.turikhay.tlauncher.ui.block.BlockableLayeredPane;
import com.turikhay.tlauncher.ui.swing.ResizeableComponent;

public abstract class ExtendedLayeredPane extends BlockableLayeredPane
		implements ResizeableComponent {
	private static final long serialVersionUID = -1L;

	private Integer LAYER_COUNT = 0;
	protected final Component parent;

	protected ExtendedLayeredPane() {
		this.parent = null;
	}

	protected ExtendedLayeredPane(Component parent) {
		this.parent = parent;

		if (parent == null)
			return;

		parent.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				onResize();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentShown(ComponentEvent e) {
				onResize();
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	@Override
	public Component add(Component comp) {
		super.add(comp, LAYER_COUNT++);
		return comp;
	}

	public void add(Component... components) {
		if (components == null)
			throw new NullPointerException();

		for (Component comp : components)
			add(comp);
	}

	@Override
	public void onResize() {
		if (parent == null)
			return;
		setBounds(0, 0, parent.getWidth(), parent.getHeight());

		for (Component comp : getComponents())
			if (comp instanceof ResizeableComponent)
				((ResizeableComponent) comp).onResize();
	}
}
