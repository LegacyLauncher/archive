package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class ExtendedPanel extends JPanel {
	private final List<MouseListener> mouseListeners;
	private Insets insets;

	private float opacity = 1;
	private AlphaComposite aComp;

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

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float f) {
		if(f < 0 || f > 1)
			throw new IllegalArgumentException("opacity must be in [0;1]");

		this.opacity = f;
		this.aComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f);

		repaint();
	}

	@Override
	public Insets getInsets() {
		return insets == null? super.getInsets() : insets;
	}

	public void setInsets(Insets insets) {
		this.insets = insets;
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

	@Override
	public synchronized void removeMouseListener(MouseListener listener) {
		if (listener == null)
			return;

		this.mouseListeners.remove(listener);

		for (Component comp : getComponents())
			comp.removeMouseListener(listener);
	}

	protected synchronized void removeMouseListenerOriginally(MouseListener listener) {
		super.removeMouseListener(listener);
	}

	public boolean contains(Component comp) {
		if(comp == null)
			return false;

		for(Component c : getComponents())
			if(comp.equals(c))
				return true;

		return false;
	}

	public Insets setInsets(int top, int left, int bottom, int right) {
		Insets insets = new Insets(top, left, bottom, right);
		setInsets(insets);

		return insets;
	}

	@Override
	protected void paintComponent(Graphics g0) {
		if(opacity == 1.0f) {
			super.paintComponent(g0);
			return;
		}

		Graphics2D g = (Graphics2D) g0;
		g.setComposite(aComp);

		super.paintComponent(g0);
	}

}
