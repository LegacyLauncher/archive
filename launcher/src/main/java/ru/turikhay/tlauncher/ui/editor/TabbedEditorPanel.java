package ru.turikhay.tlauncher.ui.editor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.TabbedPane;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TabbedEditorPanel extends AbstractEditorPanel {
    private static final Logger LOGGER = LogManager.getLogger(TabbedEditorPanel.class);

    private static final Point zeroPoint = new Point(0, 0);

    protected final BorderPanel container;
    protected final TabbedPane tabPane;
    protected final List<TabbedEditorPanel.EditorPanelTab> tabs;

    public TabbedEditorPanel(CenterPanelTheme theme, Insets insets) {
        super(theme, insets);
        tabs = new ArrayList<>();

        TabbedPane tabbedPane;
        tabbedPane = new TabbedPane() {
            @Override
            public void onTabChange(int index) {
                super.onTabChange(index);
                TabbedEditorPanel.this.onTabChange(index);
            }
        };
        tabPane = tabbedPane;

        if (tabPane.getExtendedUI() != null) {
            tabPane.getExtendedUI().setTheme(getTheme());
        }

        container = new BorderPanel();
        container.setNorth(messagePanel);
        container.setCenter(tabPane);
        setLayout(new BorderLayout());
        super.add(container, "Center");
    }

    private void onTabChange(int index) {
        if (index < tabs.size()) {
            EditorPanelTab tab = tabs.get(index);
            tab.onSelected();
        }
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

    public TabbedPane getTabPane() {
        return tabPane;
    }

    protected void add(TabbedEditorPanel.EditorPanelTab tab) {
        if (tab == null) {
            throw new NullPointerException("tab is null");
        } else {
            tabPane.addTab(tab.getTabName(), tab.getTabIcon(), tab.getScroll(), tab.getTabTip());
            tabs.add(tab);
        }
    }

    protected void remove(TabbedEditorPanel.EditorPanelTab tab) {
        if (tab == null) {
            throw new NullPointerException("tab is null");
        } else {
            int index = tabs.indexOf(tab);
            if (index != -1) {
                tabPane.removeTabAt(index);
                tabs.remove(index);
            }

        }
    }

    protected int getTabOf(EditorPair pair) {
        return tabPane.indexOfComponent(pair.getPanel());
    }

    protected Del del(int aligment) {
        Color border;
        try {
            border = tabPane.getExtendedUI().getTheme().getBorder();
        } catch (Exception var4) {
            border = getTheme().getBorder();
        }

        return new Del(1, aligment, border);
    }

    public class EditorPanelTab extends ExtendedPanel implements LocalizableComponent {
        private final String name;
        private final String tip;
        private final Icon icon;
        private final List<ExtendedPanel> panels;
        private final List<GridBagConstraints> constraints;
        private byte paneNum;
        private byte rowNum;
        private final TabbedEditorPanel.EditorScrollPane scroll;
        private boolean savingEnabled;

        public EditorPanelTab(String name, String tip, Icon icon) {
            savingEnabled = true;
            if (name == null) {
                throw new NullPointerException();
            } else if (name.isEmpty()) {
                throw new IllegalArgumentException("name is empty");
            } else {
                this.name = name;
                this.tip = tip;
                this.icon = icon;
                panels = new ArrayList<>();
                constraints = new ArrayList<>();
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
                setInsets(0, 10, 0, 10);
                scroll = new EditorScrollPane(this);
            }
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

        public TabbedEditorPanel.EditorScrollPane getScroll() {
            return scroll;
        }

        public boolean getSavingEnabled() {
            return savingEnabled;
        }

        public void setSavingEnabled(boolean b) {
            savingEnabled = b;
        }

        public void add(EditorPair pair, int rows) {
            LocalizableLabel label = pair.getLabel();
            ExtendedPanel field = pair.getPanel();
            ExtendedPanel panel;
            GridBagConstraints c;
            if (paneNum == panels.size()) {
                panel = new ExtendedPanel(new GridBagLayout());
                c = new GridBagConstraints();
                c.insets = new Insets(2, 0, 2, 0);
                c.fill = 2;
                add(panel, del(0));
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

        public void add(EditorPair pair) {
            add(pair, 1);
        }

        public void nextPane() {
            rowNum = 0;
            ++paneNum;
        }

        public void updateLocale() {
            int index = tabPane.indexOfComponent(scroll);
            if (index == -1) {
                throw new RuntimeException("Cannot find scroll component in tabPane for tab: " + name);
            } else {
                tabPane.setTitleAt(index, getTabName());
                tabPane.setToolTipTextAt(index, getTabTip());
            }
        }

        protected void onSelected() {
            scroll.getViewport().setViewPosition(zeroPoint);
        }
    }

    public static class EditorScrollPane extends ScrollPane {
        private final TabbedEditorPanel.EditorPanelTab tab;

        EditorScrollPane(TabbedEditorPanel.EditorPanelTab tab) {
            super(tab);
            this.tab = tab;
        }

        public TabbedEditorPanel.EditorPanelTab getTab() {
            return tab;
        }
    }
}
