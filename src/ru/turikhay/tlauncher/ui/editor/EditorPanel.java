package ru.turikhay.tlauncher.ui.editor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class EditorPanel extends CenterPanel {
	private static final long serialVersionUID = 3428243378644563729L;
	
	protected final ExtendedPanel container;
	protected final ScrollPane scroll;
	
	private final List<ExtendedPanel> panels;
	private final List<GridBagConstraints> constraints;
	
	protected final List<EditorHandler> handlers;
	private byte paneNum, rowNum;
	
	public EditorPanel(Insets insets) {
		super(insets);
		
		this.container = new ExtendedPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		
		this.panels = new ArrayList<ExtendedPanel>();
		this.constraints = new ArrayList<GridBagConstraints>();
		
		this.handlers = new ArrayList<EditorHandler>();
		
		this.scroll = new ScrollPane(container);
		add(messagePanel, scroll);
	}
	
	public EditorPanel() {
		this(squareNoTopInsets);
	}
	
	protected void add(EditorPair pair) {
		LocalizableLabel label = pair.getLabel();
		ExtendedPanel field = pair.getPanel();
		
		ExtendedPanel panel;
		GridBagConstraints c;
		
		if(paneNum == panels.size()) {
			panel = new ExtendedPanel(new GridBagLayout());
			panel.getInsets().set(0, 0, 0, 0);

			c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;

			container.add(panel, del(Del.CENTER));
			
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

		Collections.addAll(this.handlers, pair.getHandlers());
	}
	
	protected void nextPane() {
		rowNum = 0;
		paneNum++;
	}
	
	protected boolean checkValues() {
		boolean allValid = true;

		for (EditorHandler handler : handlers) {
			boolean valid = handler.isValid();

			setValid(handler, valid);
			
			if (!valid)
				allValid = false;
		}

		return allValid;
	}
	
	protected void setValid(EditorHandler handler, boolean valid) {
		Color color = valid? getTheme().getBackground() : getTheme().getFailure();
		handler.getComponent().setBackground(color);
	}
}
