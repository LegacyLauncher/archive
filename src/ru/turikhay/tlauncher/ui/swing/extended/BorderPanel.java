package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

public class BorderPanel extends ExtendedPanel {
	private static final long serialVersionUID = -7641580330557833990L;

	private BorderPanel(BorderLayout layout, boolean isDoubleBuffered) {
		super(isDoubleBuffered);

		if (layout == null)
			layout = new BorderLayout();

		setLayout(layout);
	}

	public BorderPanel() {
		this(null, true);
	}
	
	public BorderPanel(int hgap, int vgap) {
		this();
		
		this.setHgap(hgap);
		this.setVgap(vgap);
	}

	@Override
	public BorderLayout getLayout() {
		return (BorderLayout) super.getLayout();
	}

	@Override
	public void setLayout(LayoutManager mgr) {
		if (mgr instanceof BorderLayout)
			super.setLayout(mgr);
	}

	public int getHgap() {
		return getLayout().getHgap();
	}

	public void setHgap(int hgap) {
		getLayout().setHgap(hgap);
	}

	public int getVgap() {
		return getLayout().getVgap();
	}

	public void setVgap(int vgap) {
		getLayout().setVgap(vgap);
	}
	
	public void setNorth(Component comp) {
		add(comp, BorderLayout.NORTH);
	}
	
	public void setEast(Component comp) {
		add(comp, BorderLayout.EAST);
	}
	
	public void setSouth(Component comp) {
		add(comp, BorderLayout.SOUTH);
	}
	
	public void setWest(Component comp) {
		add(comp, BorderLayout.WEST);
	}
	
	public void setCenter(Component comp) {
		add(comp, BorderLayout.CENTER);
	}
}
