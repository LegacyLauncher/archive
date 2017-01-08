package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;
import ru.turikhay.tlauncher.ui.center.DefaultCenterPanelTheme;
import ru.turikhay.tlauncher.ui.loc.LocalizableInvalidateTextField;
import ru.turikhay.util.OS;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class SearchField extends LocalizableInvalidateTextField {
    private static final long serialVersionUID = -6453744340240419870L;
    private static final CenterPanelTheme darkTheme = new DefaultCenterPanelTheme() {
        public final Color backgroundColor = new Color(0, 0, 0, 255);
        public final Color focusColor = new Color(255, 255, 255, 255);
        public final Color focusLostColor = new Color(128, 128, 128, 255);
        public final Color successColor;

        {
            successColor = focusColor;
        }

        public Color getBackground() {
            return backgroundColor;
        }

        public Color getFocus() {
            return focusColor;
        }

        public Color getFocusLost() {
            return focusLostColor;
        }

        public Color getSuccess() {
            return successColor;
        }
    };

    SearchField(final SearchPanel sp) {
        super("logger.search.placeholder");
        if (OS.WINDOWS.isCurrent()) {
            setTheme(darkTheme);
        }

        setText(null);
        setCaretColor(Color.white);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sp.search();
            }
        });
    }
}
