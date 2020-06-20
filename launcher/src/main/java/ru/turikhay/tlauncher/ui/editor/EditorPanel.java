package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditorPanel extends AbstractEditorPanel {
    private static final long serialVersionUID = 3428243378644563729L;
    protected final ExtendedPanel container;
    protected final ScrollPane scroll;
    private final List<ExtendedPanel> panels;
    private final List<GridBagConstraints> constraints;
    protected final List<EditorHandler> handlers;
    private byte paneNum;
    private byte rowNum;

    public EditorPanel(Insets insets) {
        super(insets);
        container = new ExtendedPanel();
        container.setLayout(new BoxLayout(container, 3));
        panels = new ArrayList();
        constraints = new ArrayList();
        handlers = new ArrayList();
        scroll = new ScrollPane(container);
        add(messagePanel, scroll);
    }

    public EditorPanel() {
        this(smallSquareNoTopInsets);
    }

    protected void add(EditorPair pair) {
        LocalizableLabel label = pair.getLabel();
        ExtendedPanel field = pair.getPanel();
        ExtendedPanel panel;
        GridBagConstraints c;
        if (paneNum == panels.size()) {
            panel = new ExtendedPanel(new GridBagLayout());
            panel.getInsets().set(0, 0, 0, 0);
            c = new GridBagConstraints();
            c.fill = 2;
            container.add(panel, del(0));
            panels.add(panel);
            constraints.add(c);
        } else {
            panel = panels.get(paneNum);
            c = constraints.get(paneNum);
        }

        c.anchor = 17;
        c.gridy = rowNum;
        c.gridx = 0;
        c.weightx = 0.1D;
        panel.add(label, c);
        c.anchor = 13;
        c.gridy = rowNum++;
        c.gridx = 1;
        c.weightx = 1.0D;
        panel.add(field, c);
        handlers.addAll(pair.getHandlers());
    }

    protected void nextPane() {
        rowNum = 0;
        ++paneNum;
    }
}
