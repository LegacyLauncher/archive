package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JComponent;
import ru.turikhay.tlauncher.ui.block.BlockableLayeredPane;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;

public abstract class ExtendedLayeredPane extends BlockableLayeredPane implements ResizeableComponent {
   private static final long serialVersionUID = -1L;
   private Integer LAYER_COUNT = 0;
   protected final JComponent parent;

   protected ExtendedLayeredPane() {
      this.parent = null;
   }

   protected ExtendedLayeredPane(JComponent parent) {
      this.parent = parent;
      if (parent != null) {
         parent.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
               ExtendedLayeredPane.this.onResize();
            }

            public void componentMoved(ComponentEvent e) {
            }

            public void componentShown(ComponentEvent e) {
               ExtendedLayeredPane.this.onResize();
            }

            public void componentHidden(ComponentEvent e) {
            }
         });
      }

   }

   public Component add(Component comp) {
      Integer var10002 = this.LAYER_COUNT;
      this.LAYER_COUNT = this.LAYER_COUNT + 1;
      super.add(comp, var10002);
      return comp;
   }

   public void add(Component... components) {
      if (components == null) {
         throw new NullPointerException();
      } else {
         Component[] var5 = components;
         int var4 = components.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component comp = var5[var3];
            this.add(comp);
         }

      }
   }

   public void onResize() {
      if (this.parent != null) {
         this.setSize(this.parent.getWidth(), this.parent.getHeight());
         Component[] var4;
         int var3 = (var4 = this.getComponents()).length;

         for(int var2 = 0; var2 < var3; ++var2) {
            Component comp = var4[var2];
            if (comp instanceof ResizeableComponent) {
               ((ResizeableComponent)comp).onResize();
            }
         }
      }

   }
}
