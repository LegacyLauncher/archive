package ru.turikhay.tlauncher.ui.console;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;
import ru.turikhay.tlauncher.ui.center.DefaultCenterPanelTheme;
import ru.turikhay.tlauncher.ui.loc.LocalizableInvalidateTextField;
import ru.turikhay.util.OS;

class SearchField extends LocalizableInvalidateTextField {
	private static final long serialVersionUID = -6453744340240419870L;

	private static final CenterPanelTheme darkTheme = new DefaultCenterPanelTheme() {
		public final Color backgroundColor = new Color(0, 0, 0, 255);

		public final Color focusColor = new Color(255, 255, 255, 255);
		public final Color focusLostColor = new Color(128, 128, 128, 255); // Gray

		public final Color successColor = focusColor;

		@Override
		public Color getBackground() {
			return backgroundColor;
		}

		@Override
		public Color getFocus() {
			return focusColor;
		}

		@Override
		public Color getFocusLost() {
			return focusLostColor;
		}

		@Override
		public Color getSuccess() {
			return successColor;
		}
	};

	SearchField(final SearchPanel sp) {
		super("console.search.placeholder");
		
		if(OS.WINDOWS.isCurrent()) // Works correctly only under Windows -_-
			this.setTheme(darkTheme);

		this.setText(null);
		this.setCaretColor(Color.white);

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sp.search();
			}
		});
	}
}
