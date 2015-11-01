package ru.turikhay.tlauncher.ui.console;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.loc.LocalizableInvalidateTextField;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class SearchPanel extends ExtendedPanel {
   final ConsoleFrame cf;
   public final SearchPanel.SearchField field;
   public final SearchPanel.SearchPrefs prefs;
   public final SearchPanel.FindButton find;
   public final SearchPanel.KillButton kill;
   private int startIndex;
   private int endIndex;
   private String lastText;
   private boolean lastRegexp;

   SearchPanel(ConsoleFrame cf) {
      this.cf = cf;
      this.field = new SearchPanel.SearchField();
      this.prefs = new SearchPanel.SearchPrefs();
      this.find = new SearchPanel.FindButton();
      this.kill = new SearchPanel.KillButton();
      GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      layout.setAutoCreateContainerGaps(true);
      layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.field).addComponent(this.prefs)).addGap(4).addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(this.find, 48, 48, Integer.MAX_VALUE).addComponent(this.kill)));
      layout.linkSize(0, new Component[]{this.find, this.kill});
      layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.field).addComponent(this.find, 24, 24, Integer.MAX_VALUE)).addGap(2).addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(this.prefs).addComponent(this.kill)));
      layout.linkSize(1, new Component[]{this.field, this.prefs, this.find, this.kill});
   }

   void search() {
   }

   private void focus() {
      this.field.requestFocusInWindow();
   }

   public class SearchPrefs extends BorderPanel {
      public final LocalizableCheckbox regexp;

      private SearchPrefs() {
         this.regexp = new LocalizableCheckbox("console.search.prefs.regexp");
         SearchPanel.this.field.setFont(this.regexp.getFont());
         this.setWest(this.regexp);
      }

      public boolean getUseRegExp() {
         return this.regexp.isSelected();
      }

      public void setUseRegExp(boolean use) {
         this.regexp.setSelected(use);
      }

      // $FF: synthetic method
      SearchPrefs(Object x1) {
         this();
      }
   }

   public class SearchField extends LocalizableInvalidateTextField {
      private SearchField() {
         super("console.search.placeholder");
         this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               SearchPanel.this.search();
            }
         });
      }

      // $FF: synthetic method
      SearchField(Object x1) {
         this();
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
         return this.start > 0 && this.end > this.start;
      }
   }

   public class KillButton extends ImageButton {
      private KillButton() {
      }

      // $FF: synthetic method
      KillButton(Object x1) {
         this();
      }
   }

   public class FindButton extends ImageButton {
      private FindButton() {
         this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               SearchPanel.this.search();
            }
         });
      }

      // $FF: synthetic method
      FindButton(Object x1) {
         this();
      }
   }
}
