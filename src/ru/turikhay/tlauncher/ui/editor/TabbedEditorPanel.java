package ru.turikhay.tlauncher.ui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import ru.turikhay.tlauncher.ui.center.CenterPanelTheme;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.TabbedPane;

public class TabbedEditorPanel extends AbstractEditorPanel {
   private static final Point zeroPoint = new Point(0, 0);
   protected final BorderPanel container;
   protected final TabbedPane tabPane = new TabbedPane() {
      public void onTabChange(int index) {
         super.onTabChange(index);
         if (index < TabbedEditorPanel.this.tabs.size()) {
            TabbedEditorPanel.EditorPanelTab tab = (TabbedEditorPanel.EditorPanelTab)TabbedEditorPanel.this.tabs.get(index);
            tab.onSelected();
         }

      }
   };
   protected final List tabs = new ArrayList();

   public TabbedEditorPanel(CenterPanelTheme theme, Insets insets) {
      super(theme, insets);
      if (this.tabPane.getExtendedUI() != null) {
         this.tabPane.getExtendedUI().setTheme(this.getTheme());
      }

      this.container = new BorderPanel();
      this.container.setNorth(this.messagePanel);
      this.container.setCenter(this.tabPane);
      this.setLayout(new BorderLayout());
      super.add(this.container, "Center");
   }

   protected void add(TabbedEditorPanel.EditorPanelTab tab) {
      if (tab == null) {
         throw new NullPointerException("tab is null");
      } else {
         this.tabPane.addTab(tab.getTabName(), tab.getTabIcon(), tab.getScroll(), tab.getTabTip());
         this.tabs.add(tab);
      }
   }

   protected void remove(TabbedEditorPanel.EditorPanelTab tab) {
      if (tab == null) {
         throw new NullPointerException("tab is null");
      } else {
         int index = this.tabs.indexOf(tab);
         if (index != -1) {
            this.tabPane.removeTabAt(index);
            this.tabs.remove(index);
         }

      }
   }

   protected Del del(int aligment) {
      Color border;
      try {
         border = this.tabPane.getExtendedUI().getTheme().getBorder();
      } catch (Exception var4) {
         border = this.getTheme().getBorder();
      }

      return new Del(1, aligment, border);
   }

   public class EditorScrollPane extends ScrollPane {
      private final TabbedEditorPanel.EditorPanelTab tab;

      EditorScrollPane(TabbedEditorPanel.EditorPanelTab tab) {
         super(tab);
         this.tab = tab;
      }

      public TabbedEditorPanel.EditorPanelTab getTab() {
         return this.tab;
      }
   }

   public class EditorPanelTab extends ExtendedPanel implements LocalizableComponent {
      private final String name;
      private final String tip;
      private final Icon icon;
      private final List panels;
      private final List constraints;
      private byte paneNum;
      private byte rowNum;
      private final TabbedEditorPanel.EditorScrollPane scroll;
      private boolean savingEnabled;

      public EditorPanelTab(String name, String tip, Icon icon) {
         this.savingEnabled = true;
         if (name == null) {
            throw new NullPointerException();
         } else if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
         } else {
            this.name = name;
            this.tip = tip;
            this.icon = icon;
            this.panels = new ArrayList();
            this.constraints = new ArrayList();
            this.setLayout(new BoxLayout(this, 3));
            this.setInsets(0, 10, 0, 10);
            this.scroll = TabbedEditorPanel.this.new EditorScrollPane(this);
         }
      }

      public EditorPanelTab(String name) {
         this(name, (String)null, (Icon)null);
      }

      public String getTabName() {
         return Localizable.get(this.name);
      }

      public Icon getTabIcon() {
         return this.icon;
      }

      public String getTabTip() {
         return Localizable.get(this.tip);
      }

      public TabbedEditorPanel.EditorScrollPane getScroll() {
         return this.scroll;
      }

      public boolean getSavingEnabled() {
         return this.savingEnabled;
      }

      public void setSavingEnabled(boolean b) {
         this.savingEnabled = b;
      }

      public void add(EditorPair pair, int rows) {
         LocalizableLabel label = pair.getLabel();
         ExtendedPanel field = pair.getPanel();
         ExtendedPanel panel;
         GridBagConstraints c;
         if (this.paneNum == this.panels.size()) {
            panel = new ExtendedPanel(new GridBagLayout());
            c = new GridBagConstraints();
            c.insets = new Insets(2, 0, 2, 0);
            c.fill = 2;
            this.add(panel, TabbedEditorPanel.this.del(0));
            this.panels.add(panel);
            this.constraints.add(c);
         } else {
            panel = (ExtendedPanel)this.panels.get(this.paneNum);
            c = (GridBagConstraints)this.constraints.get(this.paneNum);
         }

         c.anchor = 17;
         c.gridy = this.rowNum;
         c.gridx = 0;
         c.weightx = 0.1D;
         panel.add(label, c);
         c.anchor = 13;
         byte var10003 = this.rowNum;
         this.rowNum = (byte)(var10003 + 1);
         c.gridy = var10003;
         c.gridx = 1;
         c.weightx = 1.0D;
         panel.add(field, c);
         TabbedEditorPanel.this.handlers.addAll(pair.getHandlers());
      }

      public void add(EditorPair pair) {
         this.add(pair, 1);
      }

      public void nextPane() {
         this.rowNum = 0;
         ++this.paneNum;
      }

      public void updateLocale() {
         int index = TabbedEditorPanel.this.tabPane.indexOfComponent(this.scroll);
         if (index == -1) {
            throw new RuntimeException("Cannot find scroll component in tabPane for tab: " + this.name);
         } else {
            TabbedEditorPanel.this.tabPane.setTitleAt(index, this.getTabName());
            TabbedEditorPanel.this.tabPane.setToolTipTextAt(index, this.getTabTip());
         }
      }

      protected void onSelected() {
         this.scroll.getViewport().setViewPosition(TabbedEditorPanel.zeroPoint);
      }
   }
}
