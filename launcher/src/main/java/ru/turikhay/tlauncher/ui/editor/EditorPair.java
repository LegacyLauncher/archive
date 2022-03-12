package ru.turikhay.tlauncher.ui.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class EditorPair {

    private final LocalizableLabel label;
    private final List<? extends EditorHandler> handlers;
    private final ExtendedPanel panel;

    public EditorPair(String labelPath, List<? extends EditorHandler> handlers) {
        this.handlers = handlers;
        label = new LocalizableLabel(labelPath);

        panel = new ExtendedPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        //c.gridwidth = GridBagConstraints.;
        c.gridy = -1;
        EditorHandler prev = null;
        for (EditorHandler handler : handlers) {
            if (NEXT_COLUMN.equals(handler)) {
                c.gridx++;
                prev = handler;
                continue;
            } else if (!NEXT_COLUMN.equals(prev)) {
                c.gridx = 0;
                c.gridy++;
            }

            JComponent comp = handler.getComponent();
            comp.setAlignmentX(0.0f);

            panel.add(comp, c);

            prev = handler;
        }
    }

    public EditorPair(String labelPath, EditorHandler... handlers) {
        this(labelPath, Arrays.asList(handlers));
    }

    public List<? extends EditorHandler> getHandlers() {
        return handlers;
    }

    public LocalizableLabel getLabel() {
        return label;
    }

    public ExtendedPanel getPanel() {
        return panel;
    }

    private static class NextColumn extends EditorHandler {
        private NextColumn() {
            super(null);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public JComponent getComponent() {
            return null;
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        protected void setValue0(String var1) {
        }

        @Override
        public void block(Object var1) {
        }

        @Override
        public void unblock(Object var1) {
        }
    }

    public static final EditorHandler NEXT_COLUMN = new NextColumn();
}
