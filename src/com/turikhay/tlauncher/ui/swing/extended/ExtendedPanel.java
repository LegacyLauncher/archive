package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class ExtendedPanel extends JPanel {
	private static final long serialVersionUID = 873670863629293560L;

	private final List<MouseListener> mouseListeners;

	public ExtendedPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);

		this.mouseListeners = new ArrayList<MouseListener>();

		setOpaque(false);
	}

	public ExtendedPanel(LayoutManager layout) {
		this(layout, true);
	}

	public ExtendedPanel(boolean isDoubleBuffered) {
		this(new FlowLayout(), isDoubleBuffered);
	}

	public ExtendedPanel() {
		this(true);
	}

	@Override
	public Component add(Component comp) {
		super.add(comp);

		if (comp == null)
			return null;

		MouseListener[] compareListeners = comp.getMouseListeners();

		for (MouseListener listener : mouseListeners) {
			MouseListener add = listener;

			for (MouseListener compareListener : compareListeners)
				if (listener.equals(compareListener)) {
					add = null;
					break;
				}

			if (add == null)
				continue;
			comp.addMouseListener(add);
		}

		return comp;
	}

	public void add(Component... components) {
		if (components == null)
			throw new NullPointerException();

		for (Component comp : components)
			add(comp);
	}

	public void add(Component component0, Component component1) {
		add(new Component[] { component0, component1 });
	}

	@Override
	public synchronized void addMouseListener(MouseListener listener) {
		if (listener == null)
			return;

		this.mouseListeners.add(listener);

		for (Component comp : getComponents())
			comp.addMouseListener(listener);
	}

	protected synchronized void addMouseListenerOriginally(
			MouseListener listener) {
		super.addMouseListener(listener);
	}

	public synchronized void removeMouseListener(MouseListener listener) {
		if (listener == null)
			return;

		this.mouseListeners.remove(listener);

		for (Component comp : getComponents())
			comp.removeMouseListener(listener);
	}

	protected synchronized void removeMouseListenerOriginally(
			MouseListener listener) {
		super.removeMouseListener(listener);
	}

}
