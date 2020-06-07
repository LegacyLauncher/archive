package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.loc.LocalizableInvalidateTextField;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchPanel extends ExtendedPanel {
    final LoggerFrame cf;
    public final SearchPanel.SearchField field;
    public final SearchPanel.SearchPrefs prefs;
    public final SearchPanel.FindButton find;
    public final SearchPanel.KillButton kill;
    private int startIndex;
    private int endIndex;
    private String lastText;
    private boolean lastRegexp;

    SearchPanel(LoggerFrame cf) {
        this.cf = cf;
        field = new SearchPanel.SearchField();
        prefs = new SearchPanel.SearchPrefs();
        find = new SearchPanel.FindButton();
        kill = new SearchPanel.KillButton();
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(field).addComponent(prefs)).addGap(4).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(find, 48, 48, Integer.MAX_VALUE).addComponent(kill)));
        layout.linkSize(0, find, kill);
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(field).addComponent(find, 24, 24, Integer.MAX_VALUE)).addGap(2).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(prefs).addComponent(kill)));
        layout.linkSize(1, field, prefs, find, kill);
    }

    void search() {
    }

    private void focus() {
        field.requestFocusInWindow();
    }

    public class FindButton extends ImageButton {
        private FindButton() {
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    search();
                }
            });
        }
    }

    public class KillButton extends ImageButton {
        private KillButton() {
        }
    }

    private class Range {
        private int start;
        private int end;

        Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        boolean isCorrect() {
            return start > 0 && end > start;
        }
    }

    public class SearchField extends LocalizableInvalidateTextField {
        private SearchField() {
            super("logger.search.placeholder");
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    search();
                }
            });
        }
    }

    public class SearchPrefs extends BorderPanel {
        public final LocalizableCheckbox regexp;

        private SearchPrefs() {
            regexp = new LocalizableCheckbox("logger.search.prefs.regexp");
            field.setFont(regexp.getFont());
            setWest(regexp);
        }

        public boolean getUseRegExp() {
            return regexp.isSelected();
        }

        public void setUseRegExp(boolean use) {
            regexp.setSelected(use);
        }
    }
}
