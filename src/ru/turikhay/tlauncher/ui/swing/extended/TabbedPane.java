package ru.turikhay.tlauncher.ui.swing.extended;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import ru.turikhay.tlauncher.ui.swing.util.Orientation;

public class TabbedPane extends JTabbedPane {
   public TabbedPane(Orientation tabLocation, TabbedPane.TabLayout layout) {
      this.setTabLocation(tabLocation == null ? Orientation.TOP : tabLocation);
      this.setTabLayout(layout == null ? TabbedPane.TabLayout.SCROLL : layout);
      TabbedPaneUI ui = this.getUI();
      if (ui instanceof WindowsTabbedPaneUI) {
         this.setUI(new WindowsTabbedPaneExtendedUI());
      }

      this.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
            TabbedPane.this.onTabChange(TabbedPane.this.getSelectedIndex());
         }
      });
   }

   public TabbedPane() {
      this((Orientation)null, (TabbedPane.TabLayout)null);
   }

   public ExtendedUI getExtendedUI() {
      TabbedPaneUI ui = this.getUI();
      return ui instanceof ExtendedUI ? (ExtendedUI)ui : null;
   }

   public void setTabLocation(Orientation direction) {
      if (direction == null) {
         throw new NullPointerException();
      } else {
         this.setTabPlacement(direction.getSwingAlias());
      }
   }

   public void setTabLayout(TabbedPane.TabLayout layout) {
      if (layout == null) {
         throw new NullPointerException();
      } else {
         this.setTabLayoutPolicy(layout.getSwingAlias());
      }
   }

   public void onTabChange(int index) {
   }

   public static enum TabLayout {
      WRAP(0),
      SCROLL(1);

      private final int swingAlias;

      private TabLayout(int swingAlias) {
         this.swingAlias = swingAlias;
      }

      public int getSwingAlias() {
         return this.swingAlias;
      }
   }
}
