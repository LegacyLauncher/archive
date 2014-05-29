package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.LayoutManager;
import ru.turikhay.tlauncher.ui.block.Unblockable;

public class UnblockablePanel extends ExtendedPanel implements Unblockable {
   private static final long serialVersionUID = -5273727580864479391L;

   public UnblockablePanel(LayoutManager layout, boolean isDoubleBuffered) {
      super(layout, isDoubleBuffered);
   }

   public UnblockablePanel(LayoutManager layout) {
      super(layout);
   }

   public UnblockablePanel(boolean isDoubleBuffered) {
      super(isDoubleBuffered);
   }

   public UnblockablePanel() {
   }
}
