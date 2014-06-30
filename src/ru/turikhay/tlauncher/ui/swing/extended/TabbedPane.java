package ru.turikhay.tlauncher.ui.swing.extended;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;
import javax.swing.JTabbedPane;
import javax.swing.plaf.ComponentUI;
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

   }

   public TabbedPane(Orientation tabLocation) {
      this(tabLocation, (TabbedPane.TabLayout)null);
   }

   public TabbedPane(TabbedPane.TabLayout layout) {
      this((Orientation)null, layout);
   }

   public TabbedPane() {
      this((Orientation)null, (TabbedPane.TabLayout)null);
   }

   public ExtendedUI getExtendedUI() {
      ComponentUI ui = this.getUI();
      return ui instanceof ExtendedUI ? (ExtendedUI)ui : null;
   }

   public Orientation getTabLocation() {
      return Orientation.fromSwingConstant(this.getTabPlacement());
   }

   public void setTabLocation(Orientation direction) {
      if (direction == null) {
         throw new NullPointerException();
      } else {
         this.setTabPlacement(direction.getSwingAlias());
      }
   }

   public TabbedPane.TabLayout getTabLayout() {
      return TabbedPane.TabLayout.fromSwingConstant(this.getTabLayoutPolicy());
   }

   public void setTabLayout(TabbedPane.TabLayout layout) {
      if (layout == null) {
         throw new NullPointerException();
      } else {
         this.setTabLayoutPolicy(layout.getSwingAlias());
      }
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

      public static TabbedPane.TabLayout fromSwingConstant(int orientation) {
         TabbedPane.TabLayout[] var4;
         int var3 = (var4 = values()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            TabbedPane.TabLayout current = var4[var2];
            if (orientation == current.getSwingAlias()) {
               return current;
            }
         }

         return null;
      }
   }
}
