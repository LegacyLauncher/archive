package ru.turikhay.tlauncher.ui.swing.extended;

import javax.swing.JTabbedPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.TabbedPaneUI;

import ru.turikhay.tlauncher.ui.swing.util.Orientation;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

public class TabbedPane extends JTabbedPane {

	public TabbedPane(Orientation tabLocation, TabLayout layout) {
		setTabLocation(tabLocation == null? Orientation.TOP : tabLocation);
		setTabLayout(layout == null? TabLayout.SCROLL : layout);

		TabbedPaneUI ui = getUI();

		if(ui instanceof WindowsTabbedPaneUI)
			setUI(new WindowsTabbedPaneExtendedUI()); // Replace with mine *not shitty bordered* UI.
	}

	public TabbedPane(Orientation tabLocation) {
		this(tabLocation, null);
	}

	public TabbedPane(TabLayout layout) {
		this(null, layout);
	}

	public TabbedPane() {
		this(null, null);
	}

	public ExtendedUI getExtendedUI() {
		ComponentUI ui = getUI();

		if(ui instanceof ExtendedUI)
			return (ExtendedUI) ui;

		return null;
	}

	public Orientation getTabLocation() {
		return Orientation.fromSwingConstant(getTabPlacement());
	}

	public void setTabLocation(Orientation direction) {
		if(direction == null)
			throw new NullPointerException();

		setTabPlacement(direction.getSwingAlias());
	}

	public TabLayout getTabLayout() {
		return TabLayout.fromSwingConstant(getTabLayoutPolicy());
	}

	public void setTabLayout(TabLayout layout) {
		if(layout == null)
			throw new NullPointerException();

		setTabLayoutPolicy(layout.getSwingAlias());
	}

	public enum TabLayout {
		WRAP(JTabbedPane.WRAP_TAB_LAYOUT), SCROLL(JTabbedPane.SCROLL_TAB_LAYOUT);

		private final int swingAlias;

		TabLayout(int swingAlias) {
			this.swingAlias = swingAlias;
		}

		public int getSwingAlias() {
			return swingAlias;
		}

		public static TabLayout fromSwingConstant(int orientation) {
			for(TabLayout current : values())
				if(orientation == current.getSwingAlias())
					return current;
			return null;
		}
	}

}
