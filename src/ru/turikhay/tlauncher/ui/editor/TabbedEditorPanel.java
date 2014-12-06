package ru.turikhay.tlauncher.ui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;

import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.TabbedPane;

public class TabbedEditorPanel extends AbstractEditorPanel {
	protected final BorderPanel container;

	protected final TabbedPane tabPane;
	protected final List<EditorPanelTab> tabs;

	public TabbedEditorPanel(CenterPanelTheme theme, Insets insets) {
		super(theme, insets);

		this.tabs = new ArrayList<EditorPanelTab>();

		this.tabPane = new TabbedPane();
		if(tabPane.getExtendedUI() != null)
			tabPane.getExtendedUI().setTheme(getTheme());

		this.container = new BorderPanel();
		container.setNorth(messagePanel);
		container.setCenter(tabPane);

		setLayout(new BorderLayout());
		super.add(container, BorderLayout.CENTER);
	}

	public TabbedEditorPanel(CenterPanelTheme theme) {
		this(theme, null);
	}

	public TabbedEditorPanel(Insets insets) {
		this(null, insets);
	}

	public TabbedEditorPanel() {
		this(smallSquareNoTopInsets);
	}

	protected void add(EditorPanelTab tab) {
		if(tab == null)
			throw new NullPointerException("tab is null");

		tabPane.addTab(tab.getTabName(), tab.getTabIcon(), tab.getScroll(), tab.getTabTip());
		tabs.add(tab);
	}

	protected int getTabOf(EditorPair pair) {
		return tabPane.indexOfComponent(pair.getPanel());
	}

	@Override
	protected Del del(int aligment) {
		Color border;

		try {
			border = tabPane.getExtendedUI().getTheme().getBorder();
		}catch(Exception e) {
			border = getTheme().getBorder();
		}

		return new Del(1, aligment, border);
	}

	public class EditorPanelTab extends ExtendedPanel implements LocalizableComponent {
		private final String name, tip;
		private final Icon icon;

		private final List<ExtendedPanel> panels;
		private final List<GridBagConstraints> constraints;

		private byte paneNum, rowNum;

		private final ScrollPane scroll;

		public EditorPanelTab(String name, String tip, Icon icon) {
			if(name == null)
				throw new NullPointerException();

			if(name.isEmpty())
				throw new IllegalArgumentException("name is empty");

			this.name = name;
			this.tip = tip;
			this.icon = icon;

			this.panels = new ArrayList<ExtendedPanel>();
			this.constraints = new ArrayList<GridBagConstraints>();

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setInsets(0, 10, 0, 10);

			this.scroll = new ScrollPane(this);
		}

		public EditorPanelTab(String name) {
			this(name, null, null);
		}

		public String getTabName() {
			return Localizable.get(name);
		}

		public Icon getTabIcon() {
			return icon;
		}

		public String getTabTip() {
			return Localizable.get(tip);
		}

		public ScrollPane getScroll() {
			return scroll;
		}

		public void add(EditorPair pair) {
			LocalizableLabel label = pair.getLabel();
			ExtendedPanel field = pair.getPanel();

			ExtendedPanel panel;
			GridBagConstraints c;

			if(paneNum == panels.size()) {
				panel = new ExtendedPanel(new GridBagLayout());
				panel.getInsets().set(0, 0, 0, 0);

				c = new GridBagConstraints();
				c.fill = GridBagConstraints.HORIZONTAL;

				add(panel, del(Del.CENTER));

				panels.add(panel);
				constraints.add(c);
			} else {
				panel = panels.get(paneNum);
				c = constraints.get(paneNum);
			}

			c.anchor = GridBagConstraints.WEST;
			c.gridy = rowNum;
			c.gridx = 0;
			c.weightx = 0.1;
			panel.add(label, c);

			c.anchor = GridBagConstraints.EAST;
			c.gridy = rowNum++;
			c.gridx = 1;
			c.weightx = 1;
			panel.add(field, c);

			Collections.addAll(handlers, pair.getHandlers());
		}

		public void nextPane() {
			rowNum = 0;
			paneNum++;
		}

		@Override
		public void updateLocale() {
			int index = tabPane.indexOfComponent(scroll);

			if(index == -1)
				throw new RuntimeException("Cannot find scroll component in tabPane for tab: "+
						name);

			tabPane.setTitleAt(index, getTabName());
			tabPane.setToolTipTextAt(index, getTabTip());
		}
	}
}
