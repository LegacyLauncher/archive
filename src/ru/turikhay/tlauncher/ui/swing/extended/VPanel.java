package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.util.OS;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.SwingUtil;

public class VPanel extends ExtendedPanel {
   private boolean magnify;
   private final Hashtable emptyPanelMap;

   private VPanel(boolean isDoubleBuffered) {
      super(isDoubleBuffered);
      this.emptyPanelMap = new Hashtable();
      this.setMagnifyGaps(true);
      this.setLayout(new BoxLayout(this, 3));
   }

   public VPanel() {
      this(true);
   }

   protected void addImpl(Component comp, Object constraints, int index) {
      super.addImpl(comp, constraints, index);
      if (this.magnify) {
         super.addImpl(new VPanel.Gap(comp), (Object)null, index);
      }

      this.checkMagnifyGaps();
   }

   public void remove(int index) {
      boolean wasGap = this.getComponent(index) instanceof VPanel.Gap;
      super.remove(index);
      if (wasGap) {
         this.checkMagnifyGaps();
      }

   }

   public final BoxLayout getLayout() {
      return (BoxLayout)super.getLayout();
   }

   public final void setLayout(LayoutManager mgr) {
      if (mgr != null && mgr instanceof BoxLayout) {
         int axis = ((BoxLayout)Reflect.cast(mgr, BoxLayout.class)).getAxis();
         if (axis != 3 && axis != 1) {
            throw new IllegalArgumentException("Illegal BoxLayout axis!");
         }

         super.setLayout(mgr);
      }

   }

   public final void setMagnifyGaps(boolean magnify) {
      this.magnify = !OS.WINDOWS.isCurrent() && TLauncherFrame.magnifyDimensions > 1.0D && magnify;
      this.checkMagnifyGaps();
   }

   protected final void checkMagnifyGaps() {
      HashMap searching = new HashMap();
      searching.putAll(this.emptyPanelMap);
      Iterator compI;
      if (this.magnify) {
         compI = searching.keySet().iterator();

         label35:
         while(true) {
            while(true) {
               if (!compI.hasNext()) {
                  break label35;
               }

               Component compE = (Component)compI.next();
               Component[] var4 = this.getComponents();
               int var5 = var4.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  Component comp = var4[var6];
                  if (comp == compE) {
                     compI.remove();
                     break;
                  }
               }
            }
         }
      }

      compI = searching.values().iterator();

      while(compI.hasNext()) {
         VPanel.Gap removal = (VPanel.Gap)compI.next();
         this.remove(removal);
      }

   }

   private class Gap extends JComponent {
      private final WeakReference comp;

      Gap(Component comp) {
         VPanel.this.emptyPanelMap.put(comp, this);
         this.comp = new WeakReference(comp);
         this.setOpaque(false);
         Dimension size = new Dimension(1, SwingUtil.magnify(2));
         this.setMinimumSize(size);
         this.setPreferredSize(size);
      }
   }
}
