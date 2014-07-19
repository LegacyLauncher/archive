package ru.turikhay.tlauncher.ui.versions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.HTMLLabel;
import ru.turikhay.util.OS;

public class VersionTipPanel extends CenterPanel implements LocalizableComponent, ResizeableComponent {
	private final HTMLLabel tip;

	VersionTipPanel(VersionHandler handler) {
		super(CenterPanel.tipTheme, CenterPanel.squareInsets);

		this.tip = new HTMLLabel();
		add(tip);

		tip.addPropertyChangeListener("html", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				Object o = evt.getNewValue();
				if(o == null || !(o instanceof View)) return;

				View view = (View) o;

				// I don't know how exactly but this fits label size to its html text. lol.
				BasicHTML.getHTMLBaseline(view, VersionHandler.ELEM_WIDTH - getHorizontalInsets(), 0);
			}

		});

		updateLocale();
	}

	@Override
	public void updateLocale() {
		tip.setText("");

		String text = Localizable.get("version.list.tip");
		if(text == null) return;

		text = text.replace("{Ctrl}", OS.OSX.isCurrent()? "Command" : "Ctrl");
		tip.setText(text);

		onResize();
	}

	@Override
	public void onResize() {
		setSize(VersionHandler.ELEM_WIDTH, tip.getHeight() + getVerticalInsets());
	}

	private int getVerticalInsets() {
		return getInsets().top + getInsets().bottom;
	}

	private int getHorizontalInsets() {
		return getInsets().left + getInsets().right;
	}
}
