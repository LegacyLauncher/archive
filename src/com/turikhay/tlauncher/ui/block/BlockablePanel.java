package com.turikhay.tlauncher.ui.block;

import com.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import java.awt.Container;
import java.awt.LayoutManager;

public class BlockablePanel extends ExtendedPanel implements Blockable {
   private static final long serialVersionUID = 1L;

   public BlockablePanel(LayoutManager layout, boolean isDoubleBuffered) {
      super(layout, isDoubleBuffered);
   }

   public BlockablePanel(LayoutManager layout) {
      super(layout);
   }

   public BlockablePanel() {
   }

   public void block(Object reason) {
      this.setEnabled(false);
      Blocker.blockComponents((Container)this, (Object)reason);
   }

   public void unblock(Object reason) {
      this.setEnabled(true);
      Blocker.unblockComponents((Container)this, (Object)reason);
   }
}
