package ru.turikhay.tlauncher.ui.console;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;

public class SearchPrefs extends JPanel {
	private static final long serialVersionUID = -5187427203445160236L;

	private LocalizableCheckbox pcase;
	private LocalizableCheckbox pwhole;
	private LocalizableCheckbox pcycle;
	private LocalizableCheckbox pregexp;

	SearchPrefs(SearchPanel sp) {
		LayoutManager layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		this.setLayout(layout);

		this.setBackground(Color.black);
		this.setForeground(Color.white);

		this.add(pcase = new LocalizableCheckbox("console.search.prefs.case"));
		this.add(pwhole = new LocalizableCheckbox("console.search.prefs.whole"));
		this.add(pcycle = new LocalizableCheckbox("console.search.prefs.cycle"));
		this.add(pregexp = new LocalizableCheckbox(
				"console.search.prefs.regexp"));

		for (Component c : this.getComponents())
			c.setForeground(Color.white);
	}

	public boolean isCaseSensetive() {
		return pcase.getState();
	}

	public boolean isWordSearch() {
		return pwhole.getState();
	}

	public boolean isCycled() {
		return pcycle.getState();
	}

	public boolean isRegExp() {
		return pregexp.getState();
	}

	public void setCaseSensetive(boolean s) {
		this.pcase.setState(s);
	}

	public void setWordSearch(boolean s) {
		this.pwhole.setState(s);
	}

	public void setCycled(boolean s) {
		this.pcycle.setState(s);
	}

	public void setRegExp(boolean s) {
		this.pregexp.setState(s);
	}

	public boolean[] get() {
		return new boolean[] { isCaseSensetive(), isWordSearch(), isCycled(),
				isRegExp() };
	}

}
