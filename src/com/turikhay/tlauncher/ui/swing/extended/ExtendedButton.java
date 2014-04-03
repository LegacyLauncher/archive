package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

public class ExtendedButton extends JButton {
	private static final long serialVersionUID = -2009736184875993130L;

	protected ExtendedButton() {
		super();
		init();
	}

	public ExtendedButton(Icon icon) {
		super(icon);
		init();
	}

	protected ExtendedButton(String text) {
		super(text);
		init();
	}

	public ExtendedButton(Action a) {
		super(a);
		init();
	}

	public ExtendedButton(String text, Icon icon) {
		super(text, icon);
		init();
	}

	private void init() {
		this.setOpaque(false);
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component parent = findRootParent(getParent());
				if (parent == null)
					return;
				parent.requestFocusInWindow();
			}

			private Component findRootParent(Component comp) {
				if (comp == null)
					return null;
				if (comp.getParent() == null)
					return comp;

				return findRootParent(comp.getParent());
			}

		});
	}

}
